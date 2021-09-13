`Samples`_

`Semantic notes`_

`Generics`_

`Plan`_

`Code generation and build process`_


Samples
-------

.. code-block::

    # minimal runtime support
    unit miod_runtime

    pub closure OnPanic(msg: String)

    pub proc set_on_panic(on_panic: OnPanic)

    # the minimal unit for language support, implicitly imported as if using importall
    unit miod_builtins

    struct _builtin
    endstruct

    struct _panic
    endstruct

    @_panic
    proc panic(msg: String)

    # to annotate structs for which to generate introspection/reflection data
    struct _reflection
    endstruct

    struct EnumType
        values: Array!<Any>
        from_string: closure(valueName: String): Any
        to_string: closure(value: Any): Any
    endstruct

    enum FieldTypeKind
        integer
        long
        float
        double
        string
        enum_type
        variant_type
        struct_type
        flags_type
    endenum

    struct FieldType
        pub name: String
        pub kind: FieldTypeKind
    endstruct
    
    struct FieldInfo
        pub name: String
        pub type: FieldType
        pub set_value: closure(instance: Any, value: Any)
        pub get_value: closure(instance: Any): Any
    endstruct

    # instanciating this type generates a compilation error
    @_builtin
    struct Any
    endstruct

    # instanciating this type generates a compilation error
    @_builtin
    struct Int
    endstruct

    # system tag annotation
    @_builtin
    struct _build_tag
        pub name: String
    endstruct

    # access all pub symbols via `std::` prefix
    import std
    # access all pub symbols without `std::`
    importall std

    # import only if tag "win32" is passed to the compiler
    @_build_tag { "win32" }
    import syswin32

    @_build_tag { "test" }
    pub proc only_for_test()
        # proc is compiled only if "test" tag is passed
    end

    @_build_tag { "ptr32" }
    alias PtrInt = Int

    @_build_tag { "ptr64" }
    alias PtrInt = Long

    # type is defined only for "debug" tag
    @_build_tag { "debug" }
    struct DebugStruct
    endstruct

    # make private proc accessible for testing
    @_build_tag { "test" }
    pub alias private_made_public = private_proc

    # cproc marks a proc as externally defined, all arguments and types are passed as for
    # normal procs so that in order to call printf, one needs to write a C wrapper function
    # which unpacks the arguments and translates String into char* etc.
    # but you cannot transform va_list, so cannot use printf etc. functions from Miod
    @_external { name: "c_puts" }
    cproc puts(s: String)

    # varargs don't make sense, since you cannot reconstruct them to modify and pass next,
    # that's why arrays are used.
    # `for` usage example:
    proc fmtstr(fmt: String, args: Array!<Any>)
        # can panic
        for i in args.iter()
            "argument N".append(i.index.str())
            match retain i.value
                case Object 
                    "this is object"
                endcase
                
                case Int
                    "this is int"
                endcase
            endmatch

            # _is_last:Bool is defined by the `for` from .has_next
            if _is_last
                puts(".")
            else
                puts(",")
            endif
        endfor

        # equivalent to the upper
        while let it = args.iter(); it.has_next()
            let i = match retain it.next().value_or_panic().value
                        case v: Int
                            v
                        else
                            0
                    endmatch
        endwhile
    end

    # compiler/hidden runtime implementation
    struct Array!<I>
        pub len: Int

        data: cpointer
    endstruct

    cprod id(any: Any): PtrInt

    # compiler/hidden runtime implementation
    cproc Array!<I>::at(index: Int): Optional!<I>

    # system-wide iterator structure, used by `for`
    struct Iterator!<I>
        next: closure(): Optional!<I>
        has_next: closure(): Bool
    endstruct

    struct ArrayIterContext
        mut i: Int
    endstruct

    struct ArrayItem!<I>
        index: Int
        value: I
    endstruct


    proc Array!<I>::iter(self): Iterator!<ArrayItem!<I>>
        let ctx = ArrayIterContext { i: 0 }
        Iterator!<I> {
            next: closure[ctx, self](): Optional!<ArrayItem!<I>>
                let item = self.at(ctx.i)
                let i = ctx.i
                if i < self.len
                    ctx.i += 1
                endif
                match item 
                    case value
                        # automatic generic args
                        Optional!<>::value { ArrayItem!<> { i, retain value.value } }
                    endcase
                    else
                        # automatic generic args
                        Optional!<>::empty
                endmatch
            endclosure
            has_next: closure[ctx, self](): Bool
                ctx.i < self.len
            endclosure
        }
    end
    

    alias Int = Int32
    # public struct type
    pub struct Object
        # public mutable field
        pub mut a: Int, setter set_a # value type

        # field is not allocated, not assigned
        @_not_stored
        s: String, getter get_s # reference type

        # private writable on initialization var
        c: Int
    endstruct

    proc Object::get_s(self): String
        "hello"
    end

    proc Object::set_a(self, a: Int)
        self.a = a
    end

    pub variant Optional!<A>
        empty,
        value { value: A }
    endvariant
    
    @_panic
    proc Optional!<A>::value_or_panic(self): A
        match self
            case value
                retain self.value
            endcase
        else
            panic("Optional is empty.")
        endmatch
    end
    
    proc Optional!<A>::value_or_default(self, default: A): A
        match self
            case value
                retain self.value
            endcase
        else
            default
        endmatch
    end
    enum Days
        working
        holiday
    endenum
    
    const global_const = "aaa"

    flags Access
        read
        write
    endflags

    # declare proc_addr type titled `Callback`
    pub proc_addr Callback(x: Int): Int
    # declare closure type name
    pub closure CallbackClosure(): Bool

    proc Object::calc(self)
        self.a + self.c
    end

    proc Object::new(c: Int): Object
        Object {
            a: 0,
            c: c,
        }
    end

    proc Object::_op_equals(self, other: Object): Bool
        self.a == other.a && self.c == other.c
    end

    # must generate compilation error when any of _op_* called or passed in code
    # also trigger error if initial underscore is used in naming except in std library
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
            endcase
        endmatch

        match any
            case Object
                any.a = 77
            endcase
        endmatch
    end

    proc closure_sample()
        let o = Object::new(3)
        let c = "aaa"
        let cl =  closure[weak o, c](x: Int): Bool
            false
        endclosure
    end

    @_deep_eq
    struct Deep
        s: String
        o: Object
    endstruct

    proc array_sample()
        # [..,] -- syntactic sugar to construct Array!<> instance
        let a = [1,2,3]

        # mutable array has set_value_at(self, value, index)
        let ma = MutableArray!<>::from([1, 2, 3])
        ma.set_value_at(5, 1)
        assert(ma.at(1).value_or_fail(), 5)

        let da = DynamicArray!<>::from([1, 2, 3])
        da.append(5)
    end

    # closure to allow some context, like constants/salts etc
    closure HashProc!<K>(item: K): Int

    struct HashMap!<K, V>
        pub len: Int

        hash_proc: HashProc!<K>
    endstruct

    struct KvPair!<K, V>
        pub key: K
        pub value: V
    endstruct

    proc HashMap!<K, V>::new(hash_proc: HashProc!<K>)
    end

    alias StringHashMap!<V> = HashMap!<String, V>

    proc string_hash_map!<V>(): StringHashMap!<V>
        HashMap!<String, V>::new(closure(item: String): Int
            hash_from_string(item)
        endclosure)
    end

    proc StringHashMap!<V>::new()
        string_hash_map!<V>()
    end

    proc string_hash_map_from!<V>(items: Iterator!<KVPair!<String, V>>): HashMap!<String, V>
        let m = string_hash_map!<V>()
        m.insert_all(items)
        m
    end

    proc hash_map_sample_init()
        let hm = string_hash_map_from([HashPair!<>{"a", 1}, HashPair!<>{"b", 2}])

    end


Semantic notes
--------------

'struct' type is the only reference type, passed by pointer, uses automatic reference counting.
Variables can be annotated with 'weak', 'weak_monitor' to break ref. cycles. 'weak_monitor' is for
cache etc.

Primitive types are numeric 8..64 bit integers, floats, boolean, flags, enums, they are copied on
assignment, boxed/unboxed automatically in generics. 'retain', 'weak' keywords generate error on them.

Discarded the idea for now: Operator '==' calls '_op_equals', if it's defined or compares hidden
pointer value otherwise.  @_deep_eq annotation implements deep comparison instead.

Comparison operators are available only for numeric types. For comparing hidden pointer values
use operator 'is'. To compare string values there're procedures 'equals', 'compare'.

Identifier names with starting '_' are reserved.

Assignment operator '=' moves pointer, invalidates source pointer if 'retain' keyword is not used,
copies primitive types.

Instance is retained on assignment, on passing as proc argument, closure capture.

'_op_retain', '_op_release', '_op_free' procedures when defined can add logic triggered on
refcounter modifications.

'_op_mut_field' proc is called on mutable field being written.

Fields can have setters, getters

Private fields are accessible only from attached procs (StructName::proc_name).



Generics
--------

At the first iteration of the language, generics are implemented as syntactic sugar only. An instance
of a generic structure does not have information on the actual types it was constructed for.
Otherwise every instance would have to store that information, which includes not only the types used
for the structure but the inner types as well, e.g. an array of generic arrays: Array!<Array!<Int>>...

So when converting from an Any instance only the Any type is used for all the generic arguments:

.. code-block::

    let a = [1,2,3]

    let my_any: Any = a

    match my_any
        case Array!<Int>
            // error, will not compile!
        endcase
        case Array
            // now available iter function but as if declared as
            // returning Iterator!<ArrayItem!<Any>>
            for item in my_any.iter()
                let result = retain match item.value
                    case Int
                        item.value > 1
                    endcase
                    else
                        false
                endmatch
            endfor
        endcase
    endmatch



Plan
----

- proc
- call proc
- cproc
- let, let mut
- struct
- retain, release, weak
- annotations
- match
- enum
- variant
- closure
- flags
- for, while, if
- alias support
- imports
- global const for primitive types and strings
- generics
- alias with generics
- automatic generic types substitution based on code: let a = KVPair!<> {"a", 1}
- automatic instance construction type: let a: Array!<KVPair!<>> = [{"a", 1}, {"b", 3}]
- getters, setters, op_mut
- _op_retain, _op_release, _op_free -- must be called when operated on Any instance as well.
- _op_mut_field -- must be called when operated on Any instance, and via reflection.
- reflection & introspection
- proc_addr (needed only for optimization?)
- _op_eq, deep_eq -- optional, do we really need it? 'is, ==' vs only '==' -- python vs java style?

Code generation and build process
---------------------------------

Compiler adds system library to the unit search path. System library C code is referenced
in the cmake scripts.