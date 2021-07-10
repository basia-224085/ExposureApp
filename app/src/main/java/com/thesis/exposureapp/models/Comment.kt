package com.thesis.exposureapp.models

class Comment {
    var answer: String? = null
    var username: String? = null
    var date: String? = null
    var rating: Float = -1.0f // only for event comments

    constructor() // needed to get object from DB
}