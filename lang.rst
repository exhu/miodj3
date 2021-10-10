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


    closure FromString(valueName: String): Any
    closure AsString(value: Any): Any

    struct EnumType
        values: Array$[Any]
        from_string: FromString
        as_string: AsString
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
    
    closure SetFieldValue(instance: Any, value: Any)
    closure GetFieldValue(instance: Any): Any
    
    struct FieldInfo
        pub name: String
        pub type: FieldType
        pub set_value: SetFieldValue
        pub get_value: GetFieldValue
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
    endproc

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
    proc fmtstr(fmt: String, args: Array$[Any])
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
    endproc

    # compiler/hidden runtime implementation
    struct Array$[I]
        pub len: Int

        data: cpointer
    endstruct

    cprod id(any: Any): PtrInt

    # compiler/hidden runtime implementation
    cproc Array$[I]::at(index: Int): Optional$[I]

    # system-wide iterator structure, used by `for`
    struct Iterator$[I]
        next: closure(): Optional$[I]
        has_next: closure(): Bool
    endstruct

    struct ArrayIterContext
        mut i: Int
    endstruct

    struct ArrayItem$[I]
        index: Int
        value: I
    endstruct


    proc Array$[I]::iter(self): Iterator$[ArrayItem$[I]]
        let ctx = ArrayIterContext { i: 0 }
        Iterator$[I] {
            next: closure[ctx, self](): Optional$[ArrayItem$[I]]
                let item = self.at(ctx.i)
                let i = ctx.i
                if lt(i, self.len)
                    ctx.i = add(ctx.i, 1) # retain is implicit for fields and return values
                endif
                match item 
                    case value
                        # automatic generic args
                        Optional$[]::value { ArrayItem$[] { i, value.value } }
                    endcase
                    else
                        # automatic generic args
                        Optional$[]::empty
                endmatch
            endclosure
            has_next: closure[ctx, self](): Bool
                ctx.i < self.len
            endclosure
        }
    endproc
    

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
    endproc

    proc Object::set_a(self, a: Int)
        self.a = a
    endproc

    # initial syntax
    pub variant Optional$[A]
        empty,
        value { value: A }
    endvariant

    @_panic
    proc Optional$[A]::value_or_panic(self): A
        match self
            case value
                retain self.value
            endcase
        else
            panic("Optional is empty.")
        endmatch
    endproc
    
    proc Optional$[A]::value_or_default(self, default: A): A
        match self
            case value
                retain self.value
            endcase
        else
            default
        endmatch
    endproc
    enum Days
        working
        holiday
    endenum
    

    # simpler alternative
    struct Void
    endstruct

    # implemented as struct with one field to query type for all possible values
    # duplicate types are not checked, i.e. for Optional$[EmptyOptional]
    # at runtime the variants are Empty, Any
    pub variant Optional$[A]
        Void,
        A
    endvariant

    # generic types are not available at runtime, so need better alternative for variant
    # probably need nullability

    pub variant ParseIntResult 
        Int
        Void
    endvariant

    pub proc parse_int(s: String): ParseIntResult
    endproc

    
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
        discard +(self.a, self.c)
    endproc

    proc Object::new(c: Int): Object
        Object {
            a: 0,
            c: c,
        }
    endproc

    proc Object::_op_equals(self, other: Object): Bool
        self.a == other.a && self.c == other.c
    endproc

    # must generate compilation error when any of _op_* called or passed in code
    # also trigger error if initial underscore is used in naming except in std library
    proc Object::_op_retain(self)
    endproc

    proc Object::_op_release(self)
    endproc

    proc Object::_op_free(self)
    endproc

    proc Object::_op_mut_field(self, field_name: String)
    endproc

    proc assignment_test(b: Object, opt: Optional$[Object], any: Any)
        let a = b
        # access to b becomes invalid
        b.c
        match opt
            case value
                let c = opt.value
            endcase
        endmatch

        match any
            case Object
                any.a = 77
            endcase
        endmatch
    endproc

    proc closure_sample()
        let o = Object::new(3)
        let c = "aaa"
        let cl =  closure[weak o, c](x: Int): Bool
            false
        endclosure
    endproc

    @_deep_eq
    struct Deep
        s: String
        o: Object
    endstruct

    proc array_sample()
        # [..,] -- syntactic sugar to construct Array$[] instance
        let a = [1,2,3]

        # mutable array has set_value_at(self, value, index)
        let ma = MutableArray$[]::from([1, 2, 3])
        ma.set_value_at(5, 1)
        assert(ma.at(1).value_or_fail(), 5)

        let da = DynamicArray$[]::from([1, 2, 3])
        da.append(5)
    endproc

    # closure to allow some context, like constants/salts etc
    closure HashProc$[K](item: K): Int

    struct HashMap$[K, V]
        pub len: Int

        hash_proc: HashProc$[K]
    endstruct

    struct KvPair$[K, V]
        pub key: K
        pub value: V
    endstruct

    proc HashMap$[K, V]::new(hash_proc: HashProc$[K])
    endproc

    alias StringHashMap$[V] = HashMap$[String, V]

    proc string_hash_map$[V](): StringHashMap$[V]
        HashMap$[String, V]::new(closure(item: String): Int
            hash_from_string(item)
        endclosure)
    endproc

    proc StringHashMap$[V]::new()
        string_hash_map$[V]()
    endproc

    proc string_hash_map_from$[V](items: Iterator$[KVPair$[String, V]]): HashMap$[String, V]
        let m = string_hash_map$[V]()
        m.insert_all(items)
        m
    endproc

    proc hash_map_sample_init()
        let hm = string_hash_map_from([HashPair$[]{"a", 1}, HashPair$[]{"b", 2}])

    endproc

    # multiple procs can have the same name but different type of the first arg
    @_builtin
    cproc +(a: Int, b: Int): Int
    @_builtin
    cproc +(a: Float, b: Float): Float

    @_builtin
    cproc and(a: Boolean, b: Boolean): Boolean


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
for the structure but the inner types as well, e.g. an array of generic arrays: Array$[Array$[Int]]...

So when converting from an Any instance only the Any type is used for all the generic arguments:

.. code-block::

    let a = [1,2,3]

    let my_any: Any = a

    match my_any
        case Array$[Int]
            // error, will not compile!
        endcase
        case Array
            // now available iter function but as if declared as
            // returning Iterator$[ArrayItem$[Any]]
            for item in my_any.iter()
                let result = match item.value
                    case Int
                        gt(item.value, 1)
                    endcase
                    else
                        false
                endmatch
            endfor
        endcase
    endmatch


Match
-----

'match' keyword matches on enum, variant, type. Variant and type match reintroduce variable type.


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
- automatic generic types substitution based on code: let a = KVPair$[] {"a", 1}
- automatic instance construction type: let a: Array$[KVPair$[]] = [{"a", 1}, {"b", 3}]
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

*@_build_tag* annotation marks code for conditional compilation.
The code element is compiled only if one of the current build tags matches any from the 
associated annotations.