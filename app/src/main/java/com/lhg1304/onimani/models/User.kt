package com.lhg1304.onimani.models

import java.io.Serializable

/**
 * Created by lhg1304 on 2017-11-27.
 */

class User : Serializable {

    var uid: String? = null
    var email: String? = null
    var nickName: String? = null
    var profileUrl: String? = null
    var thumbUrl: String? = null
    var joinedDate: String? = null
    var isSelection: Boolean = false
    var latitude: Double? = null
    var longitude: Double? = null
    var memberIndex: Int? = null
}
