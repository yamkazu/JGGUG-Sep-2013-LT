package demo

class UniqueDomain {
    String value
    static constraints = {
        value unique: true
    }
}
