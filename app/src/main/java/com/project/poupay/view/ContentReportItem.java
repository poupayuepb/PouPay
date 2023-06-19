package com.project.poupay.view;

import com.project.poupay.R;

public class ContentReportItem {
    private String title;
    private double value;

    public ContentReportItem(String title, double value) {
        this.title = title;
        this.value = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getImageID() {
        int id;
        switch (title.toLowerCase()){
            case "ar-condicionado":
            case "arcondicionado":
            case "casaco":
            case "ventilador":
                id = R.drawable.ic_snowflake;
                break;
            case "impressora":
            case "scanner":
            case "multifuncional":
                id = R.drawable.ic_printer;
                break;
            case "passagem":
            case "viagem":
                id = R.drawable.ic_airplane;
                break;
            case "android":
            case "app":
            case "aplicativo":
                id = R.drawable.ic_droid;
                break;
            case "projeto":
            case "arquiteto":
                id = R.drawable.ic_archteture;
                break;

            case "papelaria":
            case "anexo":
            case "escritorio":
                id = R.drawable.ic_clips;
                break;
            case "audio":
            case "musica":
            case "cd":
            case "spotify":
                id = R.drawable.ic_audio;
                break;
            case "livro":
            case "livros":
            case "revista":
            case "hq":
            case "manga":
                id = R.drawable.ic_book;
                break;
            case "advogado":
            case "justiça":
                id = R.drawable.ic_lawer;
                break;
            case "férias":
                id = R.drawable.ic_travel;
                break;
            case "hotel":
            case "motel":
            case "estadia":
                id = R.drawable.ic_hotel;
                break;
            case "eletrodomestico":
            case "eletronico":
            case "utensilho":
                id = R.drawable.ic_eletronic;
                break;
            case "energia":
            case "eletricidade":
                id = R.drawable.ic_power;
                break;
            case "mecânico":
            case "conserto":
            case "encanador":
            case "eletricista":
            case "reparo":
                id = R.drawable.ic_wrench;
                break;
            case "mercado":
            case "supermercado":
            case "feira":
                id = R.drawable.ic_shop;
                break;


            default:
                id = R.drawable.ic_payment_other_36;
                break;

        }


        return id;
    }
}
