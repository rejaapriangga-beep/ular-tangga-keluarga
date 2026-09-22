package id.ulartangga.keluarga.game

enum class AvatarCategory(val label: String) {
    ANIMAL("Hewan"),
    ROYALTY("Kerajaan"),
    PROFESSION("Profesi")
}

enum class AnimalAvatar(val emoji: String, val label: String, val category: AvatarCategory) {
    LION("🦁", "Singa", AvatarCategory.ANIMAL),
    PANDA("🐼", "Panda", AvatarCategory.ANIMAL),
    RABBIT("🐰", "Kelinci", AvatarCategory.ANIMAL),
    DINO("🦖", "Dino", AvatarCategory.ANIMAL),
    FOX("🦊", "Rubah", AvatarCategory.ANIMAL),
    OWL("🦉", "Burung Hantu", AvatarCategory.ANIMAL),
    TIGER("🐯", "Harimau", AvatarCategory.ANIMAL),
    KOALA("🐨", "Koala", AvatarCategory.ANIMAL),
    KING("🤴", "Raja", AvatarCategory.ROYALTY),
    QUEEN("👸", "Ratu", AvatarCategory.ROYALTY),
    KNIGHT("🗡️", "Ksatria", AvatarCategory.ROYALTY),
    WIZARD("🧙", "Penyihir", AvatarCategory.ROYALTY),
    FAIRY("🧚", "Peri", AvatarCategory.ROYALTY),
    DRAGON("🐉", "Naga", AvatarCategory.ROYALTY),
    DOCTOR("🧑‍⚕️", "Dokter", AvatarCategory.PROFESSION),
    TEACHER("🧑‍🏫", "Guru", AvatarCategory.PROFESSION),
    CHEF("🧑‍🍳", "Koki", AvatarCategory.PROFESSION),
    POLICE("👮", "Polisi", AvatarCategory.PROFESSION),
    FARMER("🧑‍🌾", "Petani", AvatarCategory.PROFESSION),
    ASTRONAUT("👨‍🚀", "Astronot", AvatarCategory.PROFESSION);

    val initial: Char get() = label.first().uppercaseChar()
}
