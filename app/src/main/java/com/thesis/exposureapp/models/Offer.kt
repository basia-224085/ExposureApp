package com.thesis.exposureapp.models

import java.io.Serializable

class Offer : Serializable {
    lateinit var uid: String // owner of the offer
    lateinit var offerName: String
    lateinit var offerRecipient: String // todo to jest chyba jednak niepotrzebne
    var docId: String? = null // id of document in collection containing this offer
    var compatibility: Float? = null // used in SwipeFragment to sort offer list

    // info about the owner
    var age: Int = 0
    lateinit var gender: String
    var yearsOfExperience: Int = 0
    lateinit var country: String
    lateinit var city: String
    lateinit var traits: List<String>
    var isProfessional: Boolean = false
    lateinit var photoshoot: String // todo maybe a list of strings?

    // info about owner's needs
    var ageMinS: Int = 0
    var ageMaxS: Int = 0
    lateinit var genderS: String
    var yearsOfExperienceS: Int = 0
    lateinit var countryS: String
    lateinit var cityS: String
    lateinit var traitsS: List<String>
    var isProfessionalS: Boolean = false

    constructor() // needed to get object from DB

    constructor(
        uid: String,
        offerName: String,
        offerRecipient: String,
        age: Int,
        gender: String,
        yearsOfExperience: Int,
        country: String,
        city: String,
        traits: List<String>,
        isProfessional: Boolean,
        session: String,
        ageMinS: Int,
        ageMaxS: Int,
        genderS: String,
        yearsOfExperienceS: Int,
        countryS: String,
        cityS: String,
        traitsS: List<String>,
        isProfessionalS: Boolean
    ) {
        this.uid = uid
        this.offerName = offerName
        this.offerRecipient = offerRecipient
        this.age = age
        this.gender = gender
        this.yearsOfExperience = yearsOfExperience
        this.country = country
        this.city = city
        this.traits = traits
        this.isProfessional = isProfessional
        this.photoshoot = session
        this.ageMinS = ageMinS
        this.ageMaxS = ageMaxS
        this.genderS = genderS
        this.yearsOfExperienceS = yearsOfExperienceS
        this.countryS = countryS
        this.cityS = cityS
        this.traitsS = traitsS
        this.isProfessionalS = isProfessionalS
    }

}