package com.app.minder.domain.model

enum class Unit(val displayName: String) {
    PILL("таб"),
    PIECE("шт"),
    MG("мг"),
    ML("мл"),
    G("г"),
    DROPS("капли"),
    TABLESPOON("ст.л."),
    TEASPOON("ч.л.")
}

enum class Timing(val dasplayName: String){
    BEFORE("до еды"),
    DURING("во время еды"),
    AFTER("после еды"),
    ANY("не зависит от еды")
}