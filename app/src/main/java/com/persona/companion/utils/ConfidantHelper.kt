package com.persona.companion.utils

object ConfidantHelper {
    /**
     * Resolves the character name associated with an Arcana for a given Persona game.
     */
    fun getCharacterName(gameId: String, arcana: String): String? {
        val cleanArcana = arcana.trim()
        val g = gameId.lowercase()

        return when {
            g.startsWith("p5") -> when (cleanArcana) {
                "Fool" -> "Igor"
                "Magician" -> "Morgana"
                "Priestess" -> "Makoto Niijima"
                "Empress" -> "Haru Okumura"
                "Emperor" -> "Yusuke Kitagawa"
                "Hierophant" -> "Sojiro Sakura"
                "Lovers" -> "Ann Takamaki"
                "Chariot" -> "Ryuji Sakamoto"
                "Justice" -> "Goro Akechi"
                "Hermit" -> "Futaba Sakura"
                "Fortune" -> "Chihaya Mifune"
                "Strength" -> "Caroline & Justine"
                "Hanged-Man", "Hanged Man" -> "Munehisa Iwai"
                "Death" -> "Tae Takemi"
                "Temperance" -> "Sadayo Kawakami"
                "Devil" -> "Ichiko Ohya"
                "Tower" -> "Shinya Oda"
                "Star" -> "Hifumi Togo"
                "Moon" -> "Yuuki Mishima"
                "Sun" -> "Toranosuke Yoshida"
                "Judgement" -> "Sae Niijima"
                "Faith" -> "Kasumi Yoshizawa"
                "Councillor" -> "Takuto Maruki"
                else -> null
            }
            g.startsWith("p4") -> when {
                cleanArcana == "Fool" -> "Investigation Team"
                cleanArcana == "Magician" -> "Yosuke Hanamura"
                cleanArcana == "Priestess" -> "Yukiko Amagi"
                cleanArcana == "Empress" -> "Margaret"
                cleanArcana == "Emperor" -> "Kanji Tatsumi"
                cleanArcana == "Hierophant" -> "Ryotaro Dojima"
                cleanArcana == "Lovers" -> "Rise Kujikawa"
                cleanArcana == "Chariot" -> "Chie Satonaka"
                cleanArcana == "Justice" -> "Nanako Dojima"
                cleanArcana == "Hermit" -> "Fox (Tatsuhime Shrine)"
                cleanArcana == "Fortune" -> "Naoto Shirogane"
                cleanArcana.startsWith("Strength (Basketball)") -> "Kou Ichijo"
                cleanArcana.startsWith("Strength (Soccer)") -> "Daisuke Nagase"
                cleanArcana.startsWith("Strength") -> "Kou Ichijo / Daisuke Nagase"
                cleanArcana.startsWith("Hanged") -> "Naoki Konishi"
                cleanArcana == "Death" -> "Hisano Kuroda"
                cleanArcana == "Temperance" -> "Eri Minami"
                cleanArcana == "Devil" -> "Sayoko Uehara"
                cleanArcana == "Tower" -> "Shu Nakajima"
                cleanArcana == "Star" -> "Teddie"
                cleanArcana == "Moon" -> "Ai Ebihara"
                cleanArcana.startsWith("Sun (Drama)") -> "Yumi Ozawa"
                cleanArcana.startsWith("Sun (Band)") -> "Ayane Matsunaga"
                cleanArcana.startsWith("Sun") -> "Yumi Ozawa / Ayane Matsunaga"
                cleanArcana == "Judgement" -> "Seekers of Truth"
                cleanArcana in listOf("Jester", "Hunger") -> "Tohru Adachi"
                cleanArcana == "Aeon" -> "Marie"
                else -> null
            }
            g == "p3p_femc" -> when (cleanArcana) {
                "Fool" -> "SEES"
                "Magician" -> "Junpei Iori"
                "Priestess" -> "Fuuka Yamagishi"
                "Empress" -> "Mitsuru Kirijo"
                "Emperor" -> "Hidetoshi Odagiri"
                "Hierophant" -> "Bunkichi & Mitsuko"
                "Lovers" -> "Yukari Takeba"
                "Chariot" -> "Rio Iwasaki"
                "Justice" -> "Ken Amada"
                "Hermit" -> "Saori Hasegawa"
                "Fortune" -> "Keisuke Hiraga"
                "Strength" -> "Koromaru"
                "Hanged-Man", "Hanged Man" -> "Maiko Oohashi"
                "Death" -> "Pharos"
                "Temperance" -> "Bebe (Andre)"
                "Devil" -> "President Tanaka"
                "Tower" -> "Mutatsu (Monk)"
                "Star" -> "Akihiko Sanada"
                "Moon" -> "Shinjiro Aragaki"
                "Sun" -> "Akinari Kamiki"
                "Judgement" -> "Nyx Annihilation Team"
                "Aeon" -> "Aigis"
                else -> null
            }
            g.startsWith("p3") -> when (cleanArcana) {
                "Fool" -> "SEES"
                "Magician" -> "Kenji Tomochika"
                "Priestess" -> "Fuuka Yamagishi"
                "Empress" -> "Mitsuru Kirijo"
                "Emperor" -> "Hidetoshi Odagiri"
                "Hierophant" -> "Bunkichi & Mitsuko"
                "Lovers" -> "Yukari Takeba"
                "Chariot" -> "Kazushi Miyamoto"
                "Justice" -> "Chihiro Fushimi"
                "Hermit" -> "\"Maya\" (Isako Toriumi)"
                "Fortune" -> "Keisuke Hiraga"
                "Strength" -> "Yuko Nishiwaki"
                "Hanged-Man", "Hanged Man" -> "Maiko Oohashi"
                "Death" -> "Pharos"
                "Temperance" -> "Bebe (Andre)"
                "Devil" -> "President Tanaka"
                "Tower" -> "Mutatsu (Monk)"
                "Star" -> "Mamoru Hayase"
                "Moon" -> "Nozomi Suemitsu"
                "Sun" -> "Akinari Kamiki"
                "Judgement" -> "Nyx Annihilation Team"
                "Aeon" -> "Aigis"
                else -> null
            }
            else -> null
        }
    }
}
