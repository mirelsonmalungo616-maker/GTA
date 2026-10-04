package com.example.game.customization

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.example.game.engine.Character
import com.example.game.engine.SoundFx
import com.example.game.engine.Vehicle

data class CharacterPreset(
    val id: String,
    val name: String,
    val description: String,
    val perk: String,
    val defaultColor: Color,
    val avatarEmoji: String
)

data class PaintColorOption(
    val name: String,
    val color: Color,
    val price: Int
)

data class ModOption(
    val id: Int,
    val name: String,
    val price: Int,
    val bonus: String
)

object CustomizationManager {
    val characterPresets = listOf(
        CharacterPreset("cj", "Carl 'CJ' Johnson", "Líder e herói da Grove Street Families", "+25% Agilidade & Fôlego de Corrida", Color(0xFF1B4D3E), "👑"),
        CharacterPreset("big_smoke", "Big Smoke (Melvin Harris)", "'All you had to do was follow the damn train, CJ!'", "+35% Resistência e HP Máximo", Color(0xFF2E7D32), "🍔"),
        CharacterPreset("ryder", "Ryder (Lance Wilson)", "Gângster atirador imprevisível e veloz", "+25% Dano com SMG e Recuo Reduzido", Color(0xFF4B5320), "🕶️"),
        CharacterPreset("sweet", "Sweet Johnson", "Líder respeitado dos irmãos de Grove Street", "+30% Respeito e Dano dos Homies Recrutados", Color(0xFF1E3A8A), "🧢"),
        CharacterPreset("cesar", "Cesar Vialpando", "Líder dos Varrios Aztecas & Mestre Lowrider", "+35% Controle de Hidráulica e Drift", Color(0xFF0077B6), "🏎️"),
        CharacterPreset("tenpenny", "Oficial Frank Tenpenny", "Detetive corrupto da divisão C.R.A.S.H.", "Polícia demora o dobro para perseguir", Color(0xFF0F172A), "👮")
    )

    var selectedCharacterPresetId by mutableStateOf("cj")
    var selectedOutfitName by mutableStateOf("Grove Street Clássico")

    val paintColors = listOf(
        PaintColorOption("Preto Fosco (Matte)", Color(0xFF1A1A1A), 450),
        PaintColorOption("Vermelho Candy", Color(0xFFE63946), 600),
        PaintColorOption("Azul Meia-Noite Metálico", Color(0xFF0077B6), 650),
        PaintColorOption("Amarelo Ouro Los Santos", Color(0xFFFFB703), 800),
        PaintColorOption("Verde Neon Tóxico", Color(0xFF06D6A0), 750),
        PaintColorOption("Branco Pérola Luxo", Color(0xFFF8F9FA), 500),
        PaintColorOption("Roxo Cyberpunk", Color(0xFF7209B7), 850)
    )

    val neonColors = listOf(
        PaintColorOption("Sem Neon", Color.Transparent, 0),
        PaintColorOption("Ciano Elétrico", Color(0xFF00F5D4), 1200),
        PaintColorOption("Roxo Ultravioleta", Color(0xFF9D4EDD), 1200),
        PaintColorOption("Vermelho Carmim", Color(0xFFFF0054), 1200),
        PaintColorOption("Verde Ácido", Color(0xFF39FF14), 1200),
        PaintColorOption("Ouro Brilhante", Color(0xFFFFD166), 1400)
    )

    val engineUpgrades = listOf(
        ModOption(1, "Motor Original de Fábrica", 0, "Potência padrão"),
        ModOption(2, "Stage 2 Esportivo (+20% Aceleração)", 2500, "+20% aceleração"),
        ModOption(3, "Stage 3 Biturbo Máximo (+45% Vel/Acel)", 6500, "+45% velocidade e aceleração")
    )

    val spoilerOptions = listOf(
        ModOption(0, "Sem Aerofólio", 0, "Visual limpo"),
        ModOption(1, "Spoiler Lip Traseiro", 800, "+5% estabilidade"),
        ModOption(2, "Asa de Rua Tuner", 1800, "+12% estabilidade em curvas"),
        ModOption(3, "Asa Gigante Fibra de Carbono GT", 3500, "+25% aderência em drift")
    )

    val rimOptions = listOf(
        ModOption(0, "Rodas Originais de Fábrica", 0, "Padrão"),
        ModOption(1, "Aros Esportivos Prata Cromados", 1100, "Brilho metálico"),
        ModOption(2, "Aros Dourados VIP Estilizados", 2200, "Estilo luxo ostentação"),
        ModOption(3, "Aros Black Piano Forjados", 1600, "Design furtivo")
    )

    fun applyCharacterPreset(player: Character, presetId: String) {
        val preset = characterPresets.find { it.id == presetId } ?: return
        selectedCharacterPresetId = presetId
        player.characterSkin = presetId
        player.outfitColor = preset.defaultColor
        SoundFx.playCashChime()
    }

    fun tuneVehicleColor(player: Character, vehicle: Vehicle, paint: PaintColorOption): Boolean {
        if (player.cash >= paint.price) {
            player.cash -= paint.price
            vehicle.primaryColor = paint.color
            SoundFx.playCashChime()
            return true
        }
        return false
    }

    fun tuneVehicleNeon(player: Character, vehicle: Vehicle, neon: PaintColorOption): Boolean {
        if (player.cash >= neon.price) {
            player.cash -= neon.price
            vehicle.neonColor = if (neon.color == Color.Transparent) null else neon.color
            SoundFx.playCashChime()
            return true
        }
        return false
    }

    fun upgradeEngine(player: Character, vehicle: Vehicle, mod: ModOption): Boolean {
        if (player.cash >= mod.price) {
            player.cash -= mod.price
            vehicle.engineUpgradeStage = mod.id
            SoundFx.playCashChime()
            return true
        }
        return false
    }

    fun upgradeSpoiler(player: Character, vehicle: Vehicle, mod: ModOption): Boolean {
        if (player.cash >= mod.price) {
            player.cash -= mod.price
            vehicle.spoilerType = mod.id
            SoundFx.playCashChime()
            return true
        }
        return false
    }

    fun upgradeRims(player: Character, vehicle: Vehicle, mod: ModOption): Boolean {
        if (player.cash >= mod.price) {
            player.cash -= mod.price
            vehicle.rimType = mod.id
            SoundFx.playCashChime()
            return true
        }
        return false
    }

    fun repairAndResprayVehicle(player: Character, vehicle: Vehicle): Boolean {
        val cost = 600
        if (player.cash >= cost) {
            player.cash -= cost
            vehicle.health = vehicle.model.maxHealth
            vehicle.isDestroyed = false
            SoundFx.playCashChime()
            return true
        }
        return false
    }
}
