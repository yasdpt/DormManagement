package ir.staryas.dormmanagement.model

import com.google.gson.annotations.SerializedName


class Msg {
    @SerializedName("success")
    val success:Int? = null
    @SerializedName("message")
    val message:String? = null
}

class UsrMsg {
    @SerializedName("success")
    val success:Int? = null
    @SerializedName("message")
    val message:String? = null
    @SerializedName("admin")
    val admin:List<Admin>? = null
}

class StudentMsg {
    @SerializedName("success")
    val success:Int? = null
    @SerializedName("students")
    val students:List<Student>? = null
}

class AdminMsg {
    @SerializedName("success")
    val success: Int? = null
    @SerializedName("admins")
    val admins: List<Admin>? = null
}

class RoomMsg {
    @SerializedName("success")
    val success: Int? = null
    @SerializedName("rooms")
    val rooms: List<Room>? = null
}

class ViolationMsg {
    @SerializedName("success")
    val success: Int? = null
    @SerializedName("violations")
    val violations: List<Violation>? = null
}

class RoomStdMsg {
    @SerializedName("success")
    val success: Int? = null
    @SerializedName("termstudents")
    val termStudents: List<RoomStd>? = null
}

class TermMsg {
    @SerializedName("success")
    val success: Int? = null
    @SerializedName("terms")
    val terms: List<Term>? = null
}

class PaymentMsg {
    @SerializedName("success")
    val success: Int? = null
    @SerializedName("payments")
    val payments: List<Payment>? = null
}

class TuitionMsg {
    @SerializedName("success")
    val success: Int? = null
    @SerializedName("tuitions")
    val tuitions: List<Tuition>? = null
}

class Admin {
    @SerializedName("aid")
    val adminId: String? = null
    @SerializedName("aAL")
    val adminAccessLevel: String? = null
    @SerializedName("aname")
    val adminName: String? = null
    @SerializedName("afamily")
    val adminFamily: String? = null
    @SerializedName("ausername")
    val adminUsername: String? = null
    @SerializedName("aemail")
    val adminEmail: String? = null
    @SerializedName("aphone")
    val adminPhone: String? = null
    @SerializedName("created_at")
    val createdAt: String? = null
    @SerializedName("updated_at")
    val updatedAt: String? = null
}

data class StudentObj(var studentId: String, var studentFullName:String, var studentImage:String, var studentPId:String,
                      var studentNatId: String, var studentPhone:String, var studentDebt:String, var studentCredit:String,
                      var studentStayTerms: String, var createdAt:String, var updatedAt:String)

class Student {
    @SerializedName("sid")
    val studentId:String? = null
    @SerializedName("sfullname")
    val studentFullName:String? = null
    @SerializedName("simage")
    val studentImage:String? = null
    @SerializedName("spid")
    val studentPId:String? = null
    @SerializedName("snatid")
    val studentNatId: String? = null
    @SerializedName("sphone")
    val studentPhone: String? = null
    @SerializedName("sdebt")
    val studentDebt: String? = null
    @SerializedName("scredit")
    val studentCredit: String? = null
    @SerializedName("sstayterms")
    val studentStayTerms: String? = null
    @SerializedName("created_at")
    val createdAt: String? = null
    @SerializedName("updated_at")
    val updatedAt: String? = null
}

class Payment {
    @SerializedName("pId")
    val paymentId:String? = null
    @SerializedName("pStudentId")
    val paymentStudentId:String? = null
    @SerializedName("pAdminId")
    val paymentAdminId:String? = null
    @SerializedName("pType")
    val paymentType:String? = null
    @SerializedName("pPrice")
    val paymentPrice:String? = null
    @SerializedName("created_at")
    val createdAt:String? = null
    @SerializedName("updated_at")
    val updatedAt:String? = null
}

class Room {
    @SerializedName("roomId")
    val roomId: String? = null
    @SerializedName("roomName")
    val roomName: String? = null
    @SerializedName("roomFloor")
    val roomFloor:String? = null
    @SerializedName("roomCapacity")
    val roomCapacity:String? = null
}

class Tuition {
    @SerializedName("tuId")
    val tuId: String? = null
    @SerializedName("tuRoomCapacity")
    val tuRoomCapacity: String? = null
    @SerializedName("tuPrice")
    val tuPrice: String? = null
}

class RoomStd {
    @SerializedName("tsId")
    val tsId: String? = null
    @SerializedName("tsTerm")
    val tsTerm: String? = null
    @SerializedName("tsStudentId")
    val tsStudentId: String? = null
    @SerializedName("tsStudentName")
    val tsStudentName: String? = null
    @SerializedName("tsStudentImage")
    val tsStudentImage: String? = null
    @SerializedName("tsStudentRoomId")
    val tsStudentRoom: String? = null
    @SerializedName("tsTuitionPrice")
    val tsTuitionPrice: String? = null
    @SerializedName("tsHasPaidTuition")
    val tsHasPaidTuition: String? = null
}

class Violation {
    @SerializedName("vId")
    val violationId: String? = null
    @SerializedName("stdId")
    val studentId: String? = null
    @SerializedName("roomId")
    val roomId: String? = null
    @SerializedName("adminId")
    val adminId: String? = null
    @SerializedName("vTitle")
    val violationTitle: String? = null
    @SerializedName("vDetail")
    val violationDetail: String? = null
    @SerializedName("vCost")
    val violationCost: String? = null
    @SerializedName("vTerm")
    val violationTerm: String? = null
    @SerializedName("created_at")
    val createdAt: String? = null
    @SerializedName("updated_at")
    val updatedAt: String? = null
}

class Term {
    @SerializedName("termId")
    val termId: String? = null
    @SerializedName("termName")
    val termName: String? = null
    @SerializedName("TermStdCount")
    val termStdCount: String? = null
}