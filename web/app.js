// Halaman tamu web untuk Ular Tangga Keluarga.
// Memakai skema Firebase Realtime Database yang SAMA dengan app Android
// (lihat RoomRepository.kt) supaya bisa gabung ke room yang dibuat dari HP.

const firebaseConfig = {
  apiKey: "AIzaSyAeVUSdDFTBz8eYa2x5rPRCLj7R5EAMrnU",
  databaseURL: "https://ular-tangga-keluarga-default-rtdb.asia-southeast1.firebasedatabase.app",
  projectId: "ular-tangga-keluarga",
  appId: "1:327980925893:web:26382980ef13de4f2e81ac"
};
firebase.initializeApp(firebaseConfig);
const db = firebase.database();

const AVATARS = [
  { key: "LION", emoji: "🦁", label: "Singa" },
  { key: "PANDA", emoji: "🐼", label: "Panda" },
  { key: "RABBIT", emoji: "🐰", label: "Kelinci" },
  { key: "DINO", emoji: "🦖", label: "Dino" }
];
const COLORS = ["#E53935", "#1E88E5", "#43A047", "#FDD835"];

const LADDERS = { 2: 38, 7: 14, 8: 31, 15: 26, 21: 42, 28: 84, 36: 44, 51: 67, 71: 91, 78: 98, 87: 94 };
const SNAKES = { 16: 6, 46: 25, 49: 11, 62: 19, 64: 60, 74: 53, 89: 68, 92: 88, 95: 75, 99: 80 };
const CARD_CELLS = new Set([5, 24, 33, 40, 58, 66, 77, 85, 93]);
const MYSTERY_CELLS = new Set([3, 12, 20, 45, 55, 70, 82, 90, 97]);
const MINIGAME_CELLS = new Set([10, 18, 27, 48, 57, 65, 73, 83]);

function getDeviceId() {
  let id = localStorage.getItem("deviceId");
  if (!id) {
    id = (crypto.randomUUID ? crypto.randomUUID() : "web-" + Math.random().toString(36).slice(2) + Date.now());
    localStorage.setItem("deviceId", id);
  }
  return id;
}
const deviceId = getDeviceId();

let selectedAvatarIndex = 0;
let roomCode = null;
let lobbyPlayers = {}; // deviceId -> {name, avatar, colorIndex}
let hostId = null;
let roomStateRef = null;
let roomRefListener = null;
let stateRefListener = null;

const el = (id) => document.getElementById(id);

function renderAvatarPicker() {
  const container = el("avatarPicker");
  container.innerHTML = "";
  AVATARS.forEach((a, i) => {
    const chip = document.createElement("div");
    chip.className = "chip" + (i === selectedAvatarIndex ? " selected" : "");
    chip.textContent = a.emoji;
    chip.onclick = () => {
      selectedAvatarIndex = i;
      renderAvatarPicker();
    };
    container.appendChild(chip);
  });
}
renderAvatarPicker();

function colorIndexFor(id) {
  let hash = 0;
  for (let i = 0; i < id.length; i++) hash = (hash * 31 + id.charCodeAt(i)) >>> 0;
  return hash % COLORS.length;
}

el("joinBtn").onclick = () => {
  const name = el("nameInput").value.trim();
  const code = el("codeInput").value.trim().toUpperCase();
  el("joinError").textContent = "";

  if (!name) { el("joinError").textContent = "Isi nama dulu."; return; }
  if (!code) { el("joinError").textContent = "Isi kode room dulu."; return; }

  const roomRef = db.ref("rooms/" + code);
  roomRef.child("host").get().then((snap) => {
    if (!snap.exists()) {
      el("joinError").textContent = "Kode room tidak ditemukan.";
      return;
    }
    const avatar = AVATARS[selectedAvatarIndex];
    const payload = {
      name: name,
      avatar: avatar.key,
      colorIndex: colorIndexFor(deviceId),
      joinedAt: firebase.database.ServerValue.TIMESTAMP
    };
    roomRef.child("lobby").child(deviceId).set(payload).then(() => {
      roomCode = code;
      enterLobby();
    });
  }).catch((err) => {
    el("joinError").textContent = "Gagal gabung: " + err.message;
  });
};

function enterLobby() {
  el("joinPanel").classList.add("hidden");
  el("lobbyPanel").classList.remove("hidden");
  el("lobbyCode").textContent = roomCode;

  const roomRef = db.ref("rooms/" + roomCode);
  roomRefListener = roomRef.on("value", (snap) => {
    const val = snap.val() || {};
    hostId = val.host || null;
    lobbyPlayers = val.lobby || {};
    renderLobbyList();

    if (val.status === "playing") {
      roomRef.off("value", roomRefListener);
      enterGame();
    }
  });
}

function renderLobbyList() {
  const list = el("lobbyList");
  list.innerHTML = "";
  Object.entries(lobbyPlayers).forEach(([id, p]) => {
    const avatar = AVATARS.find((a) => a.key === p.avatar) || AVATARS[0];
    const li = document.createElement("li");
    li.textContent = avatar.emoji + " " + p.name + (id === hostId ? " (Host)" : "");
    list.appendChild(li);
  });
}

function enterGame() {
  el("lobbyPanel").classList.add("hidden");
  el("gamePanel").classList.remove("hidden");
  el("roomCodeLabel").textContent = "Room " + roomCode;

  buildBoard();

  roomStateRef = db.ref("rooms/" + roomCode + "/state");
  stateRefListener = roomStateRef.on("value", (snap) => {
    const state = snap.val();
    if (!state) return;
    renderState(state);
  });

  el("rollBtn").onclick = () => {
    db.ref("rooms/" + roomCode + "/actions").push({ deviceId: deviceId, type: "ROLL" });
  };

  el("playAgainBtn").onclick = () => {
    location.reload();
  };
}

function cellGeometry(cell) {
  const idx = Math.min(Math.max(cell - 1, 0), 99);
  const row = Math.floor(idx / 10);
  let col = idx % 10;
  if (row % 2 === 1) col = 9 - col;
  return { row, col };
}

function buildBoard() {
  const board = el("board");
  board.innerHTML = "";
  for (let cell = 1; cell <= 100; cell++) {
    const { row, col } = cellGeometry(cell);
    const div = document.createElement("div");
    const isEven = (row + col) % 2 === 0;
    div.className = "cell " + (isEven ? "light" : "dark");
    div.style.gridColumn = col + 1;
    div.style.gridRow = 10 - row;
    div.textContent = cell;

    let badge = "";
    if (LADDERS[cell]) badge = "🪜";
    else if (SNAKES[cell]) badge = "🐍";
    else if (CARD_CELLS.has(cell)) badge = "🃏";
    else if (MYSTERY_CELLS.has(cell)) badge = "❓";
    else if (MINIGAME_CELLS.has(cell)) badge = "⚡";
    if (badge) {
      const b = document.createElement("span");
      b.className = "badge";
      b.textContent = badge;
      div.appendChild(b);
    }

    board.appendChild(div);
  }
}

function tokenPosition(cell) {
  const { row, col } = cellGeometry(cell);
  const x = (col + 0.5) * 10; // percent
  const y = (9 - row + 0.5) * 10;
  return { x, y };
}

function renderState(state) {
  // dadu + giliran
  const currentPlayer = lobbyPlayers[state.currentDeviceId];
  const myTurn = state.currentDeviceId === deviceId && !state.winnerDeviceId;
  el("turnLabel").textContent = state.winnerDeviceId
    ? "Selesai"
    : currentPlayer
    ? "Giliran: " + currentPlayer.name
    : "";
  el("rollBtn").disabled = !myTurn || state.isBusy;
  el("rollBtn").textContent = myTurn ? "🎲 Lempar Dadu (" + state.diceValue + ")" : "🎲 " + state.diceValue;
  el("message").textContent = state.message || "";

  // token pemain
  const board = el("board");
  board.querySelectorAll(".token").forEach((t) => t.remove());
  const players = state.players || {};
  const ids = Object.keys(players);
  ids.forEach((id, index) => {
    const p = players[id];
    const lobby = lobbyPlayers[id];
    if (!lobby) return;
    const avatar = AVATARS.find((a) => a.key === lobby.avatar) || AVATARS[0];
    const pos = tokenPosition(p.animatedCell || p.position || 1);
    const jitterX = ids.length > 1 ? ((index % 2) - 0.5) * 2.2 : 0;
    const jitterY = ids.length > 1 ? (Math.floor(index / 2) - 0.5) * 2.2 : 0;

    const token = document.createElement("div");
    token.className = "token";
    token.style.left = (pos.x + jitterX) + "%";
    token.style.top = (pos.y + jitterY) + "%";
    token.style.background = COLORS[lobby.colorIndex % COLORS.length];
    token.textContent = avatar.emoji;
    board.appendChild(token);
  });

  // daftar pemain
  const list = el("playersList");
  list.innerHTML = "";
  ids.forEach((id) => {
    const p = players[id];
    const lobby = lobbyPlayers[id];
    if (!lobby) return;
    const avatar = AVATARS.find((a) => a.key === lobby.avatar) || AVATARS[0];
    const chip = document.createElement("div");
    chip.className = "player-chip" + (id === state.currentDeviceId ? " turn" : "");
    chip.textContent = avatar.emoji + " " + lobby.name + " · #" + (p.finished ? "🏁" : p.position);
    list.appendChild(chip);
  });

  // pemenang
  if (state.winnerDeviceId) {
    const winner = lobbyPlayers[state.winnerDeviceId];
    el("winnerText").textContent = (winner ? winner.name : "Pemain") + " menang! 🎉";
    el("winnerOverlay").classList.remove("hidden");
  } else {
    el("winnerOverlay").classList.add("hidden");
  }
}
