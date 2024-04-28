package com.example.jobswype.model

data class UserModel(
    var username: String?,
    var profilePic: String?,
    val phone: String?,
    val email: String?,
    val password: String?,
    val role: String?,
    val job_offer: String?,
    val cv: String?,
    val userId: String?,
    val liked: HashMap<String, Boolean>?,
    val aboutme: String?,
    val fcmToken: String?
){
    constructor() : this("", "", "", "", "", "", "", "", "", HashMap(), "", "")
}

