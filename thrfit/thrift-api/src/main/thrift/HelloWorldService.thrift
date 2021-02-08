namespace java com.github.yihui.rpc.thrfit.api

service HelloWorldService {
    Greeting sayHello(1: Person person);
}
struct Greeting {
1: required string message;
}

struct Person {
1: required string firstName;
2: required string lastName;
}