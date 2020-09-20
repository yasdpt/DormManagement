package ir.staryas.dormmanagement.networking

import ir.staryas.dormmanagement.model.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    // Login
    @FormUrlEncoded
    @POST("Login.php")
    fun login(@Field("mode") mode: String,@Field("username") username:String,
              @Field("password") password:String): Call<UsrMsg>


    // POST REQUESTS

    // ManageAdmins.php ADD/EDIT
    @FormUrlEncoded
    @POST("ManageAdmins.php")
    fun manageAdmins(@Field("mode") mode: String, @Field("aid") adminId: Int, @Field("aAl") adminAccessLevel:String,
                     @Field("aname") adminName:String, @Field("afamily") adminFamily:String,
                     @Field("ausername") adminUsername:String, @Field("apassword") adminPassword:String,
                     @Field("aemail") adminEmail:String, @Field("aphone") adminPhone:String) : Call<Msg>

    // ManageAdmins.php Delete
    @FormUrlEncoded
    @POST("ManageAdmins.php")
    fun deleteAdmin(@Field("mode") mode: String, @Field("aid") adminId:Int) : Call<Msg>

    // ManagePayments.php add/edit
    @FormUrlEncoded
    @POST("ManagePayments.php")
    fun managePayments(@Field("mode") mode: String, @Field("pId") pId: Int, @Field("pOldPrice") pOldPrice: Int,
                       @Field("pStudentId") pStudentId:String, @Field("pAdminId") pAdminId:Int,
                       @Field("pType") pType:String, @Field("pPrice") pPrice:Int, @Field("tsTerm") tsTerm: String) : Call<Msg>


    @FormUrlEncoded
    @POST("ManageRooms.php")
    fun manageRooms(@Field("mode") mode: String, @Field("roomId") roomId: Int,
                    @Field("rName") roomName:String, @Field("rFloor") roomFloor:Int,
                    @Field("rCapacity") roomCapacity:Int) : Call<Msg>

    @FormUrlEncoded
    @POST("ManageRooms.php")
    fun deleteRoom(@Field("mode") mode: String, @Field("roomId") roomId:Int) : Call<Msg>

    @FormUrlEncoded
    @POST("ManageStudents.php")
    fun manageStudents(@Field("mode") mode: String, @Field("sid") studentId: Int,
                       @Field("sname") sName:String, @Field("sfamily") sFamily:String,
                       @Field("simage") sImage:String, @Field("spid") sPId:String, @Field("snatid") sNatId:String,
                       @Field("sphone") sPhone:String,@Field("sdebt") sDebt:Int, @Field("scredit") sCredit:Int) : Call<Msg>


    @FormUrlEncoded
    @POST("ManageTerms.php")
    fun manageTerms(@Field("mode") mode: String,@Field("termId") termId: Int,
                    @Field("termName") termName:String) : Call<Msg>

    @FormUrlEncoded
    @POST("ManageTermStudents.php")
    fun manageTermStudents(@Field("mode") mode: String, @Field("tsOldTerm") tsOldTerm: String,
                           @Field("tsId") tsId: Int, @Field("tsTerm") tsTerm:String,
                           @Field("tsStudentId") tsStudentId:String, @Field("tsStudentRoom") tsStudentRoom: Int,
                           @Field("tsTuitionPrice") tsTuitionPrice: Int,
                           @Field("tsHasPaidTuition") tsHasPaidTuition: Int) : Call<Msg>


    @FormUrlEncoded
    @POST("ManageTermStudents.php")
    fun deleteTermStudent(@Field("mode") mode: String, @Field("tsId") tsId:Int, @Field("termName") termName: String,
                          @Field("tPrice") tPrice: Int, @Field("tsStudentId") tsStudentId: String,
                          @Field("tsHasPaidTuition") tsHasPaidTuition: Int) : Call<Msg>

    @FormUrlEncoded
    @POST("ManageViolations.php")
    fun manageViolations(@Field("mode") mode: String, @Field("tOldCost") tOldCost:Int, @Field("vId") violationId: Int, @Field("sId") studentId: String,
                         @Field("rId") roomId:Int, @Field("aId") adminId:Int, @Field("vTitle") violationTitle:String,
                         @Field("vDetail") violationDetail: String, @Field("vCost") violationCost:Int,
                         @Field("vTerm") violationTerm:String) : Call<Msg>

    @FormUrlEncoded
    @POST("ManageViolations.php")
    fun deleteViolation(@Field("mode") mode: String, @Field("vId") violationId:Int, @Field("vCost") violationCost:Int, @Field("sId") sId:String) : Call<Msg>


    @FormUrlEncoded
    @POST("UpdateTuition.php")
    fun updateTuition(@Field("tuId") tuId:Int, @Field("tuPrice") tuPrice: Int) : Call<Msg>

    @FormUrlEncoded
    @POST("ClearCredit.php")
    fun clearCredit(@Field("sId") sId: String) : Call<Msg>

    // GET REQUESTS

    @GET("GetPaymentsByStd.php")
    fun getPayments(@Query("orderby") orderBy: String, @Query("studentId") studentId: String) : Call<PaymentMsg>

    @GET("GetAdmins.php")
    fun getAdmins(@Query("orderby") orderBy:String) : Call<AdminMsg>

    @GET("GetTerms.php")
    fun getTerms(@Query("orderby") orderBy:String) : Call<TermMsg>

    @GET("GetTuitions.php")
    fun getTuitions(@Query("orderby") orderBy:String) : Call<TuitionMsg>

    @GET("GetRooms.php")
    fun getRooms(@Query("orderby") orderBy: String) : Call<RoomMsg>

    @GET("GetRoomStdByTerm.php")
    fun getRoomStdByTerm(@Query("mode") mode: String,@Query("orderby") orderBy: String,@Query("termId") termId:String, @Query("roomId") roomId:Int, @Query("studentId") studentId :String) : Call<RoomStdMsg>

    @GET("GetStudentById.php")
    fun getStudentsById(@Query("orderby") orderBy: String, @Query("studentId") studentId: String) : Call<StudentMsg>

    @GET("GetViolationsByStd.php")
    fun getViolationByStd(@Query("orderby") orderBy: String, @Query("studentId") studentId: String) : Call<ViolationMsg>

    @GET("SearchStudents.php")
    fun searchStudents(@Query("query") query:String, @Query("orderby") orderBy: String, @Query("page") page:Int) : Call<StudentMsg>
}