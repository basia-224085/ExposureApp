package com.thesis.exposureapp.ui.mix_and_match.offers

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.thesis.exposureapp.Communicator
import com.thesis.exposureapp.R
import com.thesis.exposureapp.models.Offer
import com.thesis.exposureapp.models.User

class OfferFragment : Fragment() {
    private val db: FirebaseFirestore = Firebase.firestore
    private lateinit var comm: Communicator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        comm = activity as Communicator
        // val formRole: String = arguments?.getString("formRole") as String
        val action: String = arguments?.getString("action") as String
        val root: View? = inflater.inflate(R.layout.offer_fragment, container, false)
        if (root != null) createOffer(root, action)
        return root
    }

    private fun createMenteeOffer(root: View, action: String) {
        val offerNameText: EditText = root.findViewById(R.id.offer_name)
        val menteeProfessionSpinner: Spinner = root.findViewById(R.id.mentee_profession)
        val menteeAgeText: EditText = root.findViewById(R.id.mentee_age)
        val menteeLocationSpinner: Spinner = root.findViewById(R.id.mentee_location)
        val menteeLanguagesChipGroup: ChipGroup = root.findViewById(R.id.mentee_languages)
        val menteeAboutMeText: EditText = root.findViewById(R.id.mentee_about_me)
        val menteeVisionOfCompanyText: EditText = root.findViewById(R.id.mentee_vision_of_company)
        val mentorProfessionSpinner: Spinner = root.findViewById(R.id.mentor_profession)
        val mentorMinYearsOfExperienceText: EditText =
            root.findViewById(R.id.mentor_min_years_of_experience)
        val mentorAgeRange: CrystalRangeSeekbar = root.findViewById(R.id.mentor_age_range)

        // ----------------- mentor traits ---------------------
        val mentorTraitButtonGroups: ArrayList<MaterialButtonToggleGroup> = arrayListOf(
            root.findViewById(R.id.introvert_extrovert_mentor),
            root.findViewById(R.id.teamplayer_independent_mentor),
            root.findViewById(R.id.cautious_risktaking_mentor),
            root.findViewById(R.id.organised_spontaneous_mentor),
            root.findViewById(R.id.accommodating_strongminded_mentor),
            root.findViewById(R.id.logical_emotional_mentor)
        )
        // chosen trait buttons; initialized at left button
        val mentorTraitButtons: ArrayList<MaterialButton> = arrayListOf(
            root.findViewById(R.id.introvert_mentor),
            root.findViewById(R.id.teamplayer_mentor),
            root.findViewById(R.id.cautious_mentor),
            root.findViewById(R.id.organised_mentor),
            root.findViewById(R.id.accommodating_mentor),
            root.findViewById(R.id.logical_mentor)
        )
        // check all left buttons at the beginning
        mentorTraitButtonGroups[0].check(R.id.introvert_mentor)
        mentorTraitButtonGroups[1].check(R.id.teamplayer_mentor)
        mentorTraitButtonGroups[2].check(R.id.cautious_mentor)
        mentorTraitButtonGroups[3].check(R.id.organised_mentor)
        mentorTraitButtonGroups[4].check(R.id.accommodating_mentor)
        mentorTraitButtonGroups[5].check(R.id.logical_mentor)

        // change chosen button on click
        mentorTraitButtonGroups.forEachIndexed { index, materialButtonToggleGroup ->
            materialButtonToggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
                if (isChecked) mentorTraitButtons[index] = root.findViewById(checkedId)
            }
        }
        // ----------------- mentee traits ---------------------
        val menteeTraitButtonGroups: ArrayList<MaterialButtonToggleGroup> = arrayListOf(
            root.findViewById(R.id.introvert_extrovert),
            root.findViewById(R.id.teamplayer_independent),
            root.findViewById(R.id.cautious_risktaking),
            root.findViewById(R.id.organised_spontaneous),
            root.findViewById(R.id.accommodating_strongminded),
            root.findViewById(R.id.logical_emotional)
        )
        // chosen trait buttons; initialized at left button
        val menteeTraitButtons: ArrayList<MaterialButton> = arrayListOf(
            root.findViewById(R.id.introvert),
            root.findViewById(R.id.teamplayer),
            root.findViewById(R.id.cautious),
            root.findViewById(R.id.organised),
            root.findViewById(R.id.accommodating),
            root.findViewById(R.id.logical)
        )
        // check all left buttons at the beginning
        menteeTraitButtonGroups[0].check(R.id.introvert)
        menteeTraitButtonGroups[1].check(R.id.teamplayer)
        menteeTraitButtonGroups[2].check(R.id.cautious)
        menteeTraitButtonGroups[3].check(R.id.organised)
        menteeTraitButtonGroups[4].check(R.id.accommodating)
        menteeTraitButtonGroups[5].check(R.id.logical)

        // change chosen button on click
        menteeTraitButtonGroups.forEachIndexed { index, materialButtonToggleGroup ->
            materialButtonToggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
                if (isChecked) menteeTraitButtons[index] = root.findViewById(checkedId)
            }
        }

        // populate spinners and chip groups
        val professionsArray = resources.getStringArray(R.array.Professions)
        val professionsAdapter = activity?.applicationContext?.let {
            ArrayAdapter(it, android.R.layout.simple_spinner_item, professionsArray)
        } as SpinnerAdapter
        mentorProfessionSpinner.adapter = professionsAdapter
        menteeProfessionSpinner.adapter = professionsAdapter

        val locationsArray = resources.getStringArray(R.array.Locations)
        val locationsAdapter = activity?.applicationContext?.let {
            ArrayAdapter(it, android.R.layout.simple_spinner_item, locationsArray)
        } as SpinnerAdapter
        menteeLocationSpinner.adapter = locationsAdapter

        populateChipGroup(menteeLanguagesChipGroup, resources.getStringArray(R.array.Languages))

// if offer is being edited fill with current data:
// ------------------------------------------------------------------------------------
        var currentOffer: MenteeOffer? = null
        if (action == "edit_offer") {
            currentOffer = arguments?.getSerializable("offer") as MenteeOffer?
            offerNameText.setText(currentOffer?.offerName)
            professionsArray.forEachIndexed { index, s ->
                if (s == currentOffer?.menteeProfession) menteeProfessionSpinner.setSelection(
                    index
                )
            }
            menteeAgeText.setText(currentOffer?.menteeAge.toString())
            locationsArray.forEachIndexed { index, s ->
                if (s == currentOffer?.menteeLocation) menteeLocationSpinner.setSelection(index)
            }
            currentOffer?.menteeLanguages?.forEach { language ->
                resources.getStringArray(R.array.Languages).forEachIndexed { index, s ->
                    if (s == language) {
                        menteeLanguagesChipGroup.check(index + 1)
                    }
                }
            }

            if (currentOffer?.menteeTraits?.get(0) == "introvert") menteeTraitButtonGroups[0].check(
                R.id.introvert
            )
            else menteeTraitButtonGroups[0].check(R.id.extrovert)
            if (currentOffer?.menteeTraits?.get(1) == "team player") menteeTraitButtonGroups[1].check(
                R.id.teamplayer
            )
            else menteeTraitButtonGroups[1].check(R.id.independent)
            if (currentOffer?.menteeTraits?.get(2) == "cautious") menteeTraitButtonGroups[2].check(R.id.cautious)
            else menteeTraitButtonGroups[2].check(R.id.risktaking)
            if (currentOffer?.menteeTraits?.get(3) == "organised") menteeTraitButtonGroups[3].check(
                R.id.organised
            )
            else menteeTraitButtonGroups[3].check(R.id.spontaneous)
            if (currentOffer?.menteeTraits?.get(4) == "accommodating") menteeTraitButtonGroups[4].check(
                R.id.accommodating
            )
            else menteeTraitButtonGroups[4].check(R.id.strongminded)
            if (currentOffer?.menteeTraits?.get(5) == "logical") menteeTraitButtonGroups[5].check(R.id.logical)
            else menteeTraitButtonGroups[5].check(R.id.emotional)

            menteeAboutMeText.setText(currentOffer?.menteeAboutMe)
            menteeVisionOfCompanyText.setText(currentOffer?.menteeVisionOfCompany)
            professionsArray.forEachIndexed { index, s ->
                if (s == currentOffer?.mentorProfession) mentorProfessionSpinner.setSelection(
                    index
                )
            }
            mentorMinYearsOfExperienceText.setText(currentOffer?.mentorMinYearsOfExperience.toString())
            currentOffer?.mentorMinAge?.let { mentorAgeRange.setMinStartValue(it.toFloat()) }
            currentOffer?.mentorMaxAge?.let { mentorAgeRange.setMaxStartValue(it.toFloat()) }

            if (currentOffer?.mentorTraits?.get(0) == "introvert") mentorTraitButtonGroups[0].check(
                R.id.introvert_mentor
            )
            else mentorTraitButtonGroups[0].check(R.id.extrovert_mentor)
            if (currentOffer?.mentorTraits?.get(1) == "team player") mentorTraitButtonGroups[1].check(
                R.id.teamplayer_mentor
            )
            else mentorTraitButtonGroups[1].check(R.id.independent_mentor)
            if (currentOffer?.mentorTraits?.get(2) == "cautious") mentorTraitButtonGroups[2].check(R.id.cautious_mentor)
            else mentorTraitButtonGroups[2].check(R.id.risktaking_mentor)
            if (currentOffer?.mentorTraits?.get(3) == "organised") mentorTraitButtonGroups[3].check(
                R.id.organised_mentor
            )
            else mentorTraitButtonGroups[3].check(R.id.spontaneous_mentor)
            if (currentOffer?.mentorTraits?.get(4) == "accommodating") mentorTraitButtonGroups[4].check(
                R.id.accommodating_mentor
            )
            else mentorTraitButtonGroups[4].check(R.id.strongminded_mentor)
            if (currentOffer?.mentorTraits?.get(5) == "logical") mentorTraitButtonGroups[5].check(R.id.logical_mentor)
            else mentorTraitButtonGroups[5].check(R.id.emotional_mentor)
        }
// ------------------------------------------------------------------------------------

        // after click on "save"
        val createOfferButton: Button = root.findViewById(R.id.save_offer_button)
        createOfferButton.setOnClickListener {
            val offerName: String = offerNameText.text.toString()
            val menteeProfession: String = menteeProfessionSpinner.selectedItem.toString()
            val menteeAge: Int = menteeAgeText.text.toString().toInt()
            val menteeLocation: String = menteeLocationSpinner.selectedItem.toString()
            val menteeLanguages: ArrayList<String> = ArrayList()
            val menteeTraits: ArrayList<String> = ArrayList()
            val menteeAboutMe: String = menteeAboutMeText.text.toString()
            val menteeVisionOfCompany: String = menteeVisionOfCompanyText.text.toString()
            val mentorProfession: String = mentorProfessionSpinner.selectedItem.toString()
            val mentorMinYearsOfExperience: Int =
                mentorMinYearsOfExperienceText.text.toString().toInt()
            val mentorMinAge: Int = mentorAgeRange.selectedMinValue.toInt()
            val mentorMaxAge: Int = mentorAgeRange.selectedMaxValue.toInt()
            val mentorTraits: ArrayList<String> = ArrayList()

            // fill lists from chip groups and toggle buttons
            menteeLanguagesChipGroup.checkedChipIds.forEach {
                val chip: Chip = root.findViewById(it)
                menteeLanguages.add(chip.text.toString())
            }
            mentorTraitButtons.forEach {
                mentorTraits.add(it.text.toString())
            }
            menteeTraitButtons.forEach {
                menteeTraits.add(it.text.toString())
            }

            // add offer to DB
            val menteeOffer = MenteeOffer(
                User.id,
                offerName,
                "mentee",
                menteeProfession,
                menteeAge,
                menteeLocation,
                menteeTraits,
                menteeLanguages,
                menteeAboutMe,
                menteeVisionOfCompany,
                mentorProfession,
                mentorMinYearsOfExperience,
                mentorMinAge,
                mentorMaxAge,
                mentorTraits
            )
            menteeOffer.docId = currentOffer?.docId
            saveOfferToDB(menteeOffer, action)
        }
    }

    private fun createOffer(root: View, action: String) {
        val offerNameText: EditText = root.findViewById(R.id.offer_name)
        val genderSpinner: Spinner = root.findViewById(R.id.gender)
        val yearsOfExperienceText: EditText = root.findViewById(R.id.years_of_experience)
        val ageText: EditText = root.findViewById(R.id.age)
        val countrySpinner: Spinner = root.findViewById(R.id.country)
        val citySpinner: Spinner = root.findViewById(R.id.city)
        val professionalAmateurGroup: MaterialButtonToggleGroup =
            root.findViewById(R.id.professional_amateur)
        val professionalAmateurButton: MaterialButton = root.findViewById(R.id.professional)
        professionalAmateurGroup.check(R.id.professional)
        val photoshootSpinner: Spinner = root.findViewById(R.id.photoshoot)

        val genderSSpinner: Spinner = root.findViewById(R.id.gender_s)
        val yearsOfExperienceSText: EditText = root.findViewById(R.id.years_of_experience_s)
        val ageRangeS: CrystalRangeSeekbar = root.findViewById(R.id.age_range_s)
        val professionalAmateurSGroup: MaterialButtonToggleGroup =
            root.findViewById(R.id.professional_amateur_s)
        val professionalAmateurSButton: MaterialButton = root.findViewById(R.id.professional_s)
        professionalAmateurSGroup.check(R.id.professional_s)

        // ----------------- traits ---------------------
        val traitButtonGroups: ArrayList<MaterialButtonToggleGroup> = arrayListOf(
            root.findViewById(R.id.introvert_extrovert),
            root.findViewById(R.id.teamplayer_independent),
            root.findViewById(R.id.cautious_risktaking),
            root.findViewById(R.id.organised_spontaneous),
            root.findViewById(R.id.accommodating_strongminded),
            root.findViewById(R.id.logical_emotional)
        )
        // chosen trait buttons; initialized at left button
        val traitButtons: ArrayList<MaterialButton> = arrayListOf(
            root.findViewById(R.id.introvert),
            root.findViewById(R.id.teamplayer),
            root.findViewById(R.id.cautious),
            root.findViewById(R.id.organised),
            root.findViewById(R.id.accommodating),
            root.findViewById(R.id.logical)
        )
        // check all left buttons at the beginning
        traitButtonGroups[0].check(R.id.introvert)
        traitButtonGroups[1].check(R.id.teamplayer)
        traitButtonGroups[2].check(R.id.cautious)
        traitButtonGroups[3].check(R.id.organised)
        traitButtonGroups[4].check(R.id.accommodating)
        traitButtonGroups[5].check(R.id.logical)

        // change chosen button on click
        traitButtonGroups.forEachIndexed { index, materialButtonToggleGroup ->
            materialButtonToggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
                if (isChecked) traitButtons[index] = root.findViewById(checkedId)
            }
        }
        // ----------------- traits searched ---------------------
        val traitSButtonGroups: ArrayList<MaterialButtonToggleGroup> = arrayListOf(
            root.findViewById(R.id.introvert_extrovert_s),
            root.findViewById(R.id.teamplayer_independent_s),
            root.findViewById(R.id.cautious_risktaking_s),
            root.findViewById(R.id.organised_spontaneous_s),
            root.findViewById(R.id.accommodating_strongminded_s),
            root.findViewById(R.id.logical_emotional_s)
        )
        // chosen trait buttons; initialized at left button
        val traitSButtons: ArrayList<MaterialButton> = arrayListOf(
            root.findViewById(R.id.introvert_s),
            root.findViewById(R.id.teamplayer_s),
            root.findViewById(R.id.cautious_s),
            root.findViewById(R.id.organised_s),
            root.findViewById(R.id.accommodating_s),
            root.findViewById(R.id.logical_s)
        )
        // check all left buttons at the beginning
        traitSButtonGroups[0].check(R.id.introvert_s)
        traitSButtonGroups[1].check(R.id.teamplayer_s)
        traitSButtonGroups[2].check(R.id.cautious_s)
        traitSButtonGroups[3].check(R.id.organised_s)
        traitSButtonGroups[4].check(R.id.accommodating_s)
        traitSButtonGroups[5].check(R.id.logical_s)

        // change chosen button on click
        traitSButtonGroups.forEachIndexed { index, materialButtonToggleGroup ->
            materialButtonToggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
                if (isChecked) traitSButtons[index] = root.findViewById(checkedId)
            }
        }

        // populate spinners
        val genderArray = resources.getStringArray(R.array.Gender)
        val genderAdapter = activity?.applicationContext?.let {
            ArrayAdapter(it, android.R.layout.simple_spinner_item, genderArray)
        } as SpinnerAdapter
        genderSpinner.adapter = genderAdapter
        genderSSpinner.adapter = genderAdapter

        val countryArray = resources.getStringArray(R.array.Countries)
        val countryAdapter = activity?.applicationContext?.let {
            ArrayAdapter(it, android.R.layout.simple_spinner_item, countryArray)
        } as SpinnerAdapter
        countrySpinner.adapter = countryAdapter

        val cityArray = resources.getStringArray(R.array.Cities)
        val cityAdapter = activity?.applicationContext?.let {
            ArrayAdapter(it, android.R.layout.simple_spinner_item, cityArray)
        } as SpinnerAdapter
        citySpinner.adapter = cityAdapter

        val photoshootArray = resources.getStringArray(R.array.Photoshoots)
        val photoshootAdapter = activity?.applicationContext?.let {
            ArrayAdapter(it, android.R.layout.simple_spinner_item, photoshootArray)
        } as SpinnerAdapter
        photoshootSpinner.adapter = photoshootAdapter

// if offer is being edited fill with current data:
// ------------------------------------------------------------------------------------
        var currentOffer: Offer? = null
        if (action == "edit_offer") {
            currentOffer = arguments?.getSerializable("offer") as Offer?
            offerNameText.setText(currentOffer?.offerName)
            genderArray.forEachIndexed { index, s ->
                if (s == currentOffer?.gender) genderSpinner.setSelection(index)
            }

            yearsOfExperienceText.setText(currentOffer?.yearsOfExperience.toString())
            ageText.setText(currentOffer?.age.toString())

            countryArray.forEachIndexed { index, s ->
                if (s == currentOffer?.country) countrySpinner.setSelection(index)
            }
            cityArray.forEachIndexed { index, s ->
                if (s == currentOffer?.city) citySpinner.setSelection(index)
            }

            if (currentOffer?.traits?.get(0) == "introvert") traitButtonGroups[0].check(R.id.introvert)
            else traitButtonGroups[0].check(R.id.extrovert)
            if (currentOffer?.traits?.get(1) == "team player") traitButtonGroups[1].check(R.id.teamplayer)
            else traitButtonGroups[1].check(R.id.independent)
            if (currentOffer?.traits?.get(2) == "cautious") traitButtonGroups[2].check(R.id.cautious)
            else traitButtonGroups[2].check(R.id.risktaking)
            if (currentOffer?.traits?.get(3) == "organised") traitButtonGroups[3].check(R.id.organised)
            else traitButtonGroups[3].check(R.id.spontaneous)
            if (currentOffer?.traits?.get(4) == "accommodating") traitButtonGroups[4].check(R.id.accommodating)
            else traitButtonGroups[4].check(R.id.strongminded)
            if (currentOffer?.traits?.get(5) == "logical") traitButtonGroups[5].check(R.id.logical)
            else traitButtonGroups[5].check(R.id.emotional)

            photoshootArray.forEachIndexed { index, s ->
                if (s == currentOffer?.photoshoot) photoshootSpinner.setSelection(index)
            }
            currentOffer?.ageMinS?.let { ageRangeS.setMinStartValue(it.toFloat()) }
            currentOffer?.ageMaxS?.let { ageRangeS.setMaxStartValue(it.toFloat()) }

            if (currentOffer?.traitsS?.get(0) == "introvert") traitSButtonGroups[0].check(R.id.introvert_s)
            else traitSButtonGroups[0].check(R.id.extrovert_s)
            if (currentOffer?.traitsS?.get(1) == "team player") traitSButtonGroups[1].check(R.id.teamplayer_s)
            else traitSButtonGroups[1].check(R.id.independent_s)
            if (currentOffer?.traitsS?.get(2) == "cautious") traitSButtonGroups[2].check(R.id.cautious_s)
            else traitSButtonGroups[2].check(R.id.risktaking_s)
            if (currentOffer?.traitsS?.get(3) == "organised") traitSButtonGroups[3].check(R.id.organised_s)
            else traitSButtonGroups[3].check(R.id.spontaneous_s)
            if (currentOffer?.traitsS?.get(4) == "accommodating") traitSButtonGroups[4].check(R.id.accommodating_s)
            else traitSButtonGroups[4].check(R.id.strongminded_s)
            if (currentOffer?.traitsS?.get(5) == "logical") traitSButtonGroups[5].check(R.id.logical_s)
            else traitSButtonGroups[5].check(R.id.emotional_s)
        }
// ------------------------------------------------------------------------------------
        // after click on "save"
        val saveOfferButton: Button = root.findViewById(R.id.save_offer_button)
        saveOfferButton.setOnClickListener {
            val offerName: String = offerNameText.text.toString()
            val gender: String = genderSpinner.selectedItem.toString()
            val yearsOfExperience: Int = yearsOfExperienceText.text.toString().toInt()
            val age: Int = ageText.text.toString().toInt()
            val country: String = countrySpinner.selectedItem.toString()
            val city: String = citySpinner.selectedItem.toString()
            val traits: ArrayList<String> = ArrayList()
            val genderS: String = genderSSpinner.selectedItem.toString()
            val ageMinS: Int = ageRangeS.selectedMinValue.toInt()
            val ageMaxS: Int = ageRangeS.selectedMaxValue.toInt()
            val traitsS: ArrayList<String> = ArrayList()

            // fill lists from toggle buttons
            traitButtons.forEach {
                traits.add(it.text.toString())
            }
            traitSButtons.forEach {
                traitsS.add(it.text.toString())
            }
            val offerRecipient: String
            if(User.role == resources.getString(R.string.photographer))
//TODO HERE I FINISHED LAST TIME. TO BE CONTINUED
            // add offer to DB
            val offer = Offer(
                User.id,
                offerName,
                "mentor",
                menteeProfession,
                menteeMinAge,
                menteeMaxAge,
                menteeTraits,
                mentorProfession,
                mentorYearsOfExperience,
                mentorAge,
                mentorLocation,
                mentorLanguages,
                mentorTraits,
                mentorAboutMe,
                mentorAboutMyCompany
            )
            mentorOffer.docId = currentOffer?.docId
            saveOfferToDB(mentorOffer, action)
        }
    }

    private fun saveOfferToDB(offer: Offer, action: String) {
        if (action == "add_offer") {
            db.collection("offers").add(offer)
                .addOnFailureListener {
                    Log.d("Debug", "ops! Error writing document ", it)
                }
        } else if (action == "edit_offer") {
            offer.docId?.let {
                db.collection("offers").document(it)
                    .set(offer)
                    .addOnFailureListener {
                        Log.d("Debug", "ops! Error writing document ", it)
                    }
            }
        }
        comm.replaceFragment(fragment = MixAndMatchFragment())
    }

    private fun populateChipGroup(chipGroup: ChipGroup, chipTexts: Array<String>) {
        chipTexts.forEach {
            val chip = Chip(context)
            chip.text = it
            chip.setChipBackgroundColorResource(R.color.celadon_blue)
            chip.setTextColor(resources.getColor(R.color.black))
            chip.isCheckedIconVisible = true
            chip.isCheckable = true
            chip.isClickable = true
            chip.setCheckedIconResource(R.drawable.ic_check)
            chipGroup.addView(chip)
        }
    }
}