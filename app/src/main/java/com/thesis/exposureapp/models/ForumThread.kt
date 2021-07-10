package com.thesis.exposureapp.models


import java.io.Serializable

class ForumThread : Serializable {
    var question: String? = null
    var answers: List<Comment>? = null
    var docId: String? = null // id of document in collection containing this thread

    constructor() // needed to get object from DB
}