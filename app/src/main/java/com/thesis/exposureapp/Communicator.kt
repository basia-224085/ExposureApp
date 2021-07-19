package com.thesis.exposureapp

import android.net.Uri
import androidx.fragment.app.Fragment
import com.thesis.exposureapp.models.ForumThread
import com.thesis.exposureapp.models.Offer

interface Communicator {
    fun replaceFragment(fragment: Fragment) // switch from one fragment to another in the same activity
    fun passForumThread(ft: ForumThread) // from forum to thread fragment
    fun passMatchInfo(match: Triple<String, String, Uri>)
    fun passOffer(offer: Offer)
    fun passEditInfoToForm(formRole: String, action: String, offer: Offer)
    fun passAddInfoToForm(formRole: String, action: String) // from mix and match to add offer fragment
    fun passInfoToForm(action: String, offer: Offer?)
    /*



    fun passEvent(event: Event)*/
}