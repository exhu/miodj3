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
    proc fmtstr(fmt: String, args: Array!<Any>)
        for i, a, is_last in args.iter()
            "argument N".append(i.str())
            match a
                case Object 
                    "this is object"
                end_case
                
                case Int
                    "this is int"
                end_case
            end_match
        end_for

        while false
        end_while
    end

    # compiler/hidden runtime implementation
    struct Array!<I>
        pub len: Int

        data: cpointer
    end_struct

    cprod id(any: Any): PtrInt

    # compiler/hidden runtime implementation
    cproc Array!<I>::at(index: Int): Optional!<I>

    # system-wide iterator structure, used by `for`
    struct Iterator!<I>
        next: closure(): Optional!<I>
        has_next: closure(): Bool
    end_struct

    struct ArrayIterContext
        mut i: Int
    end_struct

    proc Array!<I>::iter(self): Iterator!<I>
        let ctx = ArrayIterContext { i: 0 }
        Iterator!<I> {
            next: closure[ctx, self](): Optional!<I>
                let item = self.at(ctx.i)
                if ctx.i < self.len
                    ctx.i += 1
                end_if
                item
            end_closure
            has_next: closure[ctx, self](): Bool
                ctx.i < self.len
            end_closure
        }
    end_proc
    

    alias Int = Int32
    # public struct type
    pub struct Object
        # public mutable field
        pub mut a: Int, set set_a # value type
        s: String, get(get_s), nostore # reference type

        # private writable on initialization var
        c: Int
    end_struct

    proc Object::get_s(self): String
        "hello"
    end

    proc Object::set_a(self, a: Int)
        self.a = a
    end

    pub variant Optional!<A>
        empty,
        value { value: A }
    end_variant
    
    proc Optional!<A>::value_or_fail(self): A, panic
        match self
            case value
                self.value
            end_case
        else
            panic("Optional is empty.")
        end_match
    end
    
    proc Optional!<A>::value_or_default(self, default: A): A
        match self
            case value
                self.value
            end_case
        else
            default
        end_match
    end
    enum Days
        working
        holiday
    end_enum
    
    const global_const = "aaa"

    flags Access
        read
        write
    end_flags

    # declare proc_addr type titled `Callback`
    pub proc_addr Callback(x: Int): Int
    # declare closure type name
    pub closure CallbackClosure(): Bool

    proc Object::calc(self)
        self.a + self.c
    end_proc

    proc Object::new(c: Int): Object
        Object {
            a: 0,
            c: c,
        }
    end

    proc Object::_op_equals(self, other: Object): Bool
        self.a == other.a && self.c == other.c
    end

    proc Object::_op_retain(self)
    end

    proc Object::_op_release(self)
    end

    proc Object::_op_free(self)
    end

    proc Object::_op_mut_field(self, field_name: String)
    end

    proc assignment_test(b: Object, opt: Optional!<Object>, any: Any)
        let a = b
        # access to b becomes invalid
        b.c
        match opt
            case value
                # fails, because opt.value is not a variable
                let c = opt.value

                # correct:
                let c = retain opt.value
            end_case
        end_match

        match any
            case Object
                any.a = 77
            end_case
        end_match
    end

    proc closure_sample()
        let o = Object::new(3)
        let c = "aaa"
        let cl =  closure[weak o, c](x: Int): Bool
            false
        end_closure
    end

    @_deep_eq
    struct Deep
        s: String
        o: Object
    end_struct

    proc array_sample()
        # [..,] -- syntactic sugar to construct Array!<> instance
        let a = [1,2,3]

        # mutable array has set_value_at(self, value, index)
        let ma: MutableArray!<> = [1, 2, 3]
        ma.set_value_at(5, 1)
        assert(ma.at(1).value_or_fail(), 5)

        let da: DynamicArray!<> = [1, 2, 3]
        da.append(5)
    end

    # closure to allow some context, like constants/salts etc
    closure HashProc!<K>(item: K): Int

    struct HashMap!<K, V>
        pub len: Int

        hash_proc: HashProc!<K>
    end_struct

    struct HashPair!<K, V>
        pub key: K
        pub value: V
    end_struct

    proc HashMap!<K, V>::new(hash_proc: HashProc!<K>)
    end

    proc string_hash_map!<V>(): HashMap!<String, V>
        HashMap!<String, V>::new(closure(item: String): Int
            hash_from_string(item)
        end_closure)
    end

    proc string_hash_map_from!<V>(items: Iterator!<V>): HashMap!<String, V>
        let m = string_hash_map!<V>()
        m.insert_all(items)
        m
    end


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
