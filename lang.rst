Samples
-------

.. code-block::

    # cproc marks a proc as externally defined, all arguments and types are passed as for
    # normal procs so that in order to call printf, one needs to write a C wrapper function
    # which unpacks the arguments and translates String into char* etc.
    # but you cannot transform va_list, so cannot use printf etc. functions from Miod
    @_external { name: "puts" }
    cproc c_puts(s: String)

    # varargs don't make sense, since you cannot reconstruct them to modify and pass next,
    # that's why arrays are used.
    # `for` usage example:
    proc fmtstr(fmt: String, args: Array!<Any>) {
        for i, a, is_last in args.iter() {
            "argument N".append(i.str())
            match a {
                case Object {
                    "this is object"
                }
                case Int {
                    "this is int"
                }
            }
        }
    }

    # compiler/hidden runtime implementation
    struct Array!<I> {
        pub len: Int

        data: cpointer
    }

    # compiler/hidden runtime implementation
    cproc Array!<I>::at(index: Int): Optional!<I>

    # system-wide iterator structure, used by `for`
    struct Iterator!<I> {
        next: closure(): Optional!<I>
        has_next: closure(): Bool
    }

    struct ArrayIterContext {
        mut i: Int
    }

    proc Array!<I>::iter(self): Iterator!<I> {
        let ctx = ArrayIterContext { i: 0 }
        Iterator!<I> {
            next: closure[ctx, self](): Optional!<I> {
                let item = self.at(ctx.i)
                if ctx.i < self.len {
                    ctx.i += 1
                }
                item
            }
            has_next: closure[ctx, self](): Bool {
                ctx.i < self.len
            }
        }
    }
    

    alias Int = Int32
    # public struct type
    pub struct Object {
        # public mutable field
        pub mut a: Int, set set_a # value type
        s: String, get(get_s), nostore # reference type

        # private writable on initialization var
        c: Int
    } 

    proc Object::get_s(self): String {
        "hello"
    }

    proc Object::set_a(self, a: Int) {
        self.a = a
    }

    pub variant Optional!<A> {
        empty,
        value { value: A }
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
            c: c,
        }
    }

    proc Object::_op_equals(self, other: Object): Bool {
        self.a == other.a && self.c == other.c
    }

    proc Object::_op_retain(self) {
    }

    proc Object::_op_release(self) {
    }

    proc Object::_op_free(self) {
    }

    proc Object::_op_mut_field(self, field_name: String) {
    }

    proc assignment_test(b: Object, opt: Optional!<Object>, any: Any) {
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

        match any {
            case Object {
                any.a = 77
            }
        }
    }

    proc closure_sample() {
        let o = Object::new(3)
        let c = "aaa"
        let cl =  closure[weak o, c](x: Int): Bool {

        }
    }

    @_deep_eq
    struct Deep {
        s: String
        o: Object
    }



Semantic notes
--------------

'struct' type is the only reference type, passed by pointer, uses automatic reference counting.
Variables can be annotated with 'weak', 'weak_monitor' to break ref. cycles. 'weak_monitor' is for
cache etc.

Primitive types are numeric 8..64 bit integers, floats, boolean, flags, enums, they are copied on
assignment, boxed/unboxed automatically in generics. 'retain', 'weak' keywords generate error on them.

Operator '==' calls '_op_equals', if it's defined or compares hidden pointer value otherwise.
@_deep_eq annotation implements deep comparison instead.

Identifier names with starting '_' are reserved.

Assignment operator '=' moves pointer, invalidates source pointer if 'retain' keyword is not used,
copies primitive types.

'_op_retain', '_op_release', '_op_free' procedures when defined can add logic triggered on
refcounter modifications.

'_op_mut_field' proc is called on mutable field being written.

Fields can have setters, getters

Private fields are accessible only from attached procs (StructName::proc_name).
