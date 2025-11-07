package com.unified.healthfitness;

public class Question {
    private String question;
    private String option1, option2, option3, option4;
    private String category1, category2, category3, category4;

    public Question(String question, String option1, String option2, String option3, String option4,
                    String category1, String category2, String category3, String category4) {
        this.question = question;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.category1 = category1;
        this.category2 = category2;
        this.category3 = category3;
        this.category4 = category4;
    }

    public String getQuestion() {
        return question;
    }

    public String getOption1() {
        return option1;
    }

    public String getOption2() {
        return option2;
    }

    public String getOption3() {
        return option3;
    }

    public String getOption4() {
        return option4;
    }

    public String getCategoryForOption(int optionNumber) {
        switch (optionNumber) {
            case 1: return category1;
            case 2: return category2;
            case 3: return category3;
            case 4: return category4;
            default: return "Balanced";
        }
    }
}