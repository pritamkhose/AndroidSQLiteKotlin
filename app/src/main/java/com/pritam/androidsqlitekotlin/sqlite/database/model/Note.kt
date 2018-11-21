package com.pritam.androidsqlitekotlin.sqlite.database.model

class Note {

    var id: Int = 0
    var note: String? = null
    var timestamp: String? = null

    constructor(id: Int, note: String?, timestamp: String?) {
        this.id = id
        this.note = note
        this.timestamp = timestamp
    }

}

