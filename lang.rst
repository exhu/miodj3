Samples
-------


alias Int = Int32
# public struct type
pub struct Object {
    # public mutable field
    pub mut a: Int # value type
    s: String # reference type

    # private writable on initialization var
    c: Int
}

pub variant Optional!<A> {
    empty,
    value(value: A)
}

enum Days {
    working
    holiday
}

const global_const = "aaa"

flags Access {
    read
    write
}

pub proc_addr Callback(x: Int): Int
pub closure CallbackClosure(): Bool

proc Object::calc(self) {
    self.a + self.c
}

proc Object::new(c: Int): Object {
    Object {
        a: 0,
        s: "aaa",
        c: c,
    }
}

proc Object::op_equals(self, other: Object): Bool {
    self.a == other.a && self.c == other.c
}

proc assignment_test(b: Object, opt: Optional!<Object>) {
    let a = b
    # access to b becomes invalid
    b.c
    match opt {
        case value {
            # fails, because opt.value is not a variable
            let c = opt.value

            # correct:
            let c = retain opt.value

        }
    }
}


Semantic notes
--------------

Operator '==' calls 'op_equals', if it's defined or compares hidden pointer value otherwise.
@deep_eq annotation implements deep comparison instead.

Assignment operator '=' moves pointer, invalidates source pointer if 'retain' keyword is not used.