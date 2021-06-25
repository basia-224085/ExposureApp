package com.thesis.exposureapp.user_profile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.thesis.exposureapp.R
import com.thesis.exposureapp.authentication.AuthenticateActivity
import com.thesis.exposureapp.models.User

class UserProfileFragment: Fragment() {
    private val GALLERY_REQUEST_CODE = 100
    private lateinit var uName: TextView
    private lateinit var uSurname: TextView
    private lateinit var uRole: TextView
    private lateinit var uIntro: TextView
    private lateinit var uProfilePicture: ImageView

    private lateinit var storageReference: StorageReference
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.user_profile_fragment, container, false)
        uName = root.findViewById(R.id.name)
        uSurname = root.findViewById(R.id.surname)
        uRole = root.findViewById(R.id.role)
        uIntro = root.findViewById(R.id.intro)
        uProfilePicture = root.findViewById(R.id.profile_picture)
        storageReference = FirebaseStorage.getInstance().reference

        getUserInfoFromDB()

        // all actions of the buttons
        val editPicBtn: FloatingActionButton = root.findViewById(R.id.edit_profile_pic)
        editPicBtn.imageTintMode = null
        editPicBtn.setOnClickListener {
            popUpDeleteOrEditPicture()
        }

        val editNameBtn: ImageView = root.findViewById(R.id.edit_name)
        editNameBtn.setOnClickListener {
            popUpEditor("name")
        }

        val editSurnameBtn: ImageView = root.findViewById(R.id.edit_surname)
        editSurnameBtn.setOnClickListener {
            popUpEditor("surname")
        }

        val editRoleBtn: ImageView = root.findViewById(R.id.edit_role)
        editRoleBtn.setOnClickListener {
            popUpEditor("role")
        }
        val editIntroBtn: ImageView = root.findViewById(R.id.edit_intro)
        editIntroBtn.setOnClickListener {
            popUpEditor("intro")
        }

        val logOutBtn: Button = root.findViewById(R.id.log_out_btn)
        logOutBtn.setOnClickListener {
            auth.signOut()
            activity?.let {
                val intent = Intent(it, AuthenticateActivity::class.java)
                it.startActivity(intent)
            }
        }
        return root
    }
    // crop and upload picture
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GALLERY_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let {
                        cropImage(it)
                    }
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    result.uri?.let {
                        uploadImageToFirebase(it)
                    }
                }
            }
        }
    }

    private fun cropImage(uri: Uri) {
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1080, 1080)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .start(requireContext(), this)
    }

    private fun uploadImageToFirebase(uri: Uri?) {
        val fileReference = storageReference.child("users/${User.id}/profile_picture.jpg")
        if (uri != null) {
            // upload to firebase storage
            fileReference.putFile(uri).addOnSuccessListener {
                Picasso.get().load(uri).into(uProfilePicture)
            }
        }
    }

    private fun getUserInfoFromDB() {
        // fill fragment view with data from User Singleton
        uName.text = User.name
        uSurname.text = User.surname
        uRole.text = User.role
        uIntro.text = User.intro

        // show current profile picture from cloud storage
        val profileReference: StorageReference =
            storageReference.child("users/${User.id}/profile_picture.jpg")
        profileReference.downloadUrl.addOnSuccessListener {
            Picasso.get().load(it).into(uProfilePicture)
        }.addOnFailureListener {
            // if there is no profile pic, use default one
            val defaultReference = storageReference.child("basic/user_profile_picture.png")
            defaultReference.downloadUrl.addOnSuccessListener {
                Picasso.get().load(it).into(uProfilePicture)
            }
        }
    }

    private fun updateUserInfoInDB() {
        val newUser = hashMapOf(
            "name" to uName.text.toString(),
            "surname" to uSurname.text.toString(),
            "role" to uRole.text.toString(),
            "email" to User.email,
            "intro" to uIntro.text.toString()
        )
        val db: FirebaseFirestore = Firebase.firestore
        db.collection("users").document(User.id)
            .set(newUser)
            .addOnSuccessListener {
                Log.d(
                    "Debug",
                    "ops! DocumentSnapshot successfully written!"
                )
            }
            .addOnFailureListener { e -> Log.d("Debug", "ops! Error writing document", e) }
        // update User Singleton
        User.name = uName.text.toString()
        User.surname = uSurname.text.toString()
        User.role = uRole.text.toString()
        User.intro = uIntro.text.toString()
    }

    @SuppressLint("SetTextI18n")
    private fun popUpDeleteOrEditPicture() {
        val dialogBuilder: AlertDialog = AlertDialog.Builder(requireContext()).create()
        val inflater: LayoutInflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.popup_2_options, null)

        val editButton: Button = dialogView.findViewById(R.id.first_option_button)
        editButton.text = "Edit"
        editButton.setOnClickListener {
            dialogBuilder.dismiss()
            val galleryIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            // proceed to gallery
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
        }

        val deleteButton: Button = dialogView.findViewById(R.id.second_option_button)
        deleteButton.text = "Delete"
        deleteButton.setOnClickListener {
            dialogBuilder.dismiss()
            // delete current picture from storage
            val userReference = storageReference.child("users/${User.id}/profile_picture.jpg")
            userReference.delete().addOnSuccessListener {
                // set default picture as profile
                val defaultReference = storageReference.child("basic/user_profile_picture.png")
                defaultReference.downloadUrl.addOnSuccessListener {
                    Picasso.get().load(it).into(uProfilePicture)
                }
            }.addOnFailureListener {
                toast("Couldn't delete profile picture")
            }
        }
        dialogBuilder.setView(dialogView)
        dialogBuilder.show()
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun popUpEditor(dataType: String) {
        val dialogBuilder: AlertDialog = AlertDialog.Builder(requireContext()).create()
        val inflater: LayoutInflater = this.layoutInflater

        if (dataType == "role") {
            val dialogView: View = inflater.inflate(R.layout.popup_role_editor, null)

            val roleToEdit: Switch = dialogView.findViewById(R.id.role_editor)
            roleToEdit.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    roleToEdit.text = getString(R.string.photographer)
                } else {
                    roleToEdit.text = getString(R.string.model)
                }
            }
            roleToEdit.isChecked = uRole.text.toString() != getString(R.string.model)
            // save changes of role
            val acceptBtn: ImageView = dialogView.findViewById(R.id.accept_changes)
            acceptBtn.setOnClickListener {
                if (roleToEdit.isChecked) uRole.text = getString(R.string.photographer)
                else uRole.text = getString(R.string.model)
                updateUserInfoInDB()
                dialogBuilder.dismiss()
            }
            dialogBuilder.setView(dialogView)
        } else {
            val dialogView: View = inflater.inflate(R.layout.popup_text_editor, null)

            val textToEdit: EditText = dialogView.findViewById(R.id.text_to_edit)
            when (dataType) {
                "name" -> {
                    textToEdit.setText(uName.text)
                }
                "surname" -> {
                    textToEdit.setText(uSurname.text)
                }
                "intro" -> {
                    textToEdit.setText(uIntro.text)
                }
            }
            // save changes of name or surname
            val acceptBtn: ImageView = dialogView.findViewById(R.id.accept_changes)
            acceptBtn.setOnClickListener {
                val newVal: String = textToEdit.text.toString()
                if (TextUtils.isEmpty(newVal)) {
                    toast("Enter new value")
                } else if (dataType != "intro" && !newVal.onlyLetters()) {
                    toast("Use only letters")
                } else {
                    when (dataType) {
                        "name" -> {
                            uName.text = newVal
                        }
                        "surname" -> {
                            uSurname.text = newVal
                        }
                        "intro" -> {
                            uIntro.text = newVal
                        }
                    }
                    updateUserInfoInDB()
                    dialogBuilder.dismiss()
                }
            }
            dialogBuilder.setView(dialogView)
        }
        dialogBuilder.show()
    }

    private fun toast(toast_text: String) {
        Toast.makeText(requireContext(), toast_text, Toast.LENGTH_SHORT).show()
    }

    private fun String.onlyLetters() = all { it.isLetter() }
}