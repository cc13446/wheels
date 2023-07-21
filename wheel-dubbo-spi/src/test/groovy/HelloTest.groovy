import spock.lang.Specification

class HelloTest extends Specification{

    def "hello test"() {
        given:
        def hello = "hello"

        when:
        print(hello)

        then:
        hello == hello
    }
}
