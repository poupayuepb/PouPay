package com.projeto.poupay.view

import com.projeto.poupay.R


data class ContentReportItem(val title: String, val value: Double) {
    fun getImageID(): Int{
        val id: Int
        when (title.lowercase()) {
            "ar-condicionado", "arcondicionado", "casaco", "ventilador" -> id = R.drawable.ic_snowflake
            "impressora", "scanner", "multifuncional" -> id = R.drawable.ic_printer
            "passagem", "viagem" -> id = R.drawable.ic_airplane
            "android", "app", "aplicativo" -> id = R.drawable.ic_droid
            "projeto", "arquiteto" -> id = R.drawable.ic_archteture
            "papelaria", "anexo", "escritorio" -> id = R.drawable.ic_clips
            "audio", "musica", "cd", "spotify" -> id = R.drawable.ic_audio
            "livro", "livros", "revista", "hq", "manga" -> id = R.drawable.ic_book
            "advogado", "justiça" -> id = R.drawable.ic_lawer
            "férias" -> id = R.drawable.ic_travel
            "hotel", "motel", "estadia" -> id = R.drawable.ic_hotel
            "eletrodomestico", "eletronico", "utensilho" -> id = R.drawable.ic_eletronic
            "energia", "eletricidade" -> id = R.drawable.ic_power
            "mecânico", "conserto", "encanador", "eletricista", "reparo" -> id = R.drawable.ic_wrench
            "mercado", "supermercado", "feira" -> id = R.drawable.ic_shop
            "pix" -> id = R.drawable.ic_pix_18
            "dinheiro" -> id = R.drawable.ic_money_18
            "cartão de crédito" -> id = R.drawable.ic_card_18
            "outro" -> id = R.drawable.ic_baseline_plans_local_atm_24
            else -> id = R.drawable.ic_payment_other_36
        }
        return id
    }
}