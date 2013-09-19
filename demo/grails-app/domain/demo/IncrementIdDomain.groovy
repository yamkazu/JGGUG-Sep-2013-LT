package demo

class IncrementIdDomain {
    String value

    static mapping = {
        id generator: "increment"
    }
}
