package main.coral.evaluator

import main.coral.`object`.*

val builtins: Map<String, Builtin> = mapOf(
    "len" to Builtin(fn = { args: Array<Obj> ->
        if (args.size != 1) {
            return@Builtin newError("wrong number of arguments. got=${args.size}, want=1")
        }

        return@Builtin when (val arg = args[0]) {
            is StringOBJ -> Integer(arg.value.length)
            is ArrayList -> Integer(arg.elements.size)
            else -> newError("argument to `len` not supported, got ${arg.type()}")
        }
    }),
    "first" to Builtin(fn = { args: Array<Obj> ->
        if (args.size != 1) {
            return@Builtin newError("wrong number of arguments. got=${args.size}, want=1")
        }
        if(args[0].type() != ARRAY_OBJ) {
            return@Builtin newError("first() only support arguments of type array")
        }
        return@Builtin (args[0] as ArrayList).elements.first()
    }),
    "last" to Builtin(fn = { args: Array<Obj> ->
        if (args.size != 1) {
            return@Builtin newError("wrong number of arguments. got=${args.size}, want=1")
        }
        if(args[0].type() != ARRAY_OBJ) {
            return@Builtin newError("first() only support arguments of type array")
        }
        val lastEl = (args[0] as ArrayList).elements.last()
        return@Builtin lastEl
    }),
    "rest" to Builtin(fn = { args: Array<Obj> ->
        if (args.size != 1) {
            return@Builtin newError("wrong number of arguments. got=${args.size}, want=1")
        }
        if(args[0].type() != ARRAY_OBJ) {
            return@Builtin newError("first() only support arguments of type array")
        }
        val arr =  args[0] as ArrayList
        val length = arr.elements.size
        if (length > 0) {
            return@Builtin ArrayList(arr.elements.drop(1))
        }
        return@Builtin NULL
    }),
)
