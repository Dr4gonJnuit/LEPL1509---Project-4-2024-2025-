package com.example.jobswype.model

data class ChatModel(
    var currentUserID : String?, // Current user ID
    val sender: String?, // ID de l'expéditeur
    val recipient: String?, // ID du destinataire
    val message: String?, // Contenu du message
    val currentDate: String?, // Date du message
    val currentTime: String? // Heure du message
) {
    constructor() : this("", "", "", "", "", "")
}
