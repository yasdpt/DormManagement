package ir.staryas.dormmanagement.model

class More {
    var title:String? = null
    var backColor:Int? = null
    var image:Int? = null
    var action: String? = null

    constructor(title: String, backColor:Int, image:Int, action: String){
        this.title = title
        this.backColor = backColor
        this.image = image
        this.action = action
    }
}