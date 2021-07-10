package com.thesis.exposureapp

import android.net.Uri
import androidx.fragment.app.Fragment
import com.thesis.exposureapp.models.ForumThread

interface Communicator {
    fun replaceFragment(fragment: Fragment) // switch from one fragment to another in the same activity
    fun passForumThread(ft: ForumThread) // from forum to thread fragment
    /*fun passAddInfoToForm(formRole: String, action: String) // from mix and match to add offer fragment
    fun passEditInfoToForm(formRole: String, action: String, offer: Offer)
    fun passOffer(offer: Offer)
    fun passMatchInfo(match: Triple<String, String, Uri>)
    fun passEvent(event: Event)*/
}