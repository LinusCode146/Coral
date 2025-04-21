package main.coral.`object`

import main.coral.evaluator.NULL

class Environment(
    private val store: MutableMap<String, Obj> = mutableMapOf(),
    private val outer: Environment? = null
) {

    fun get(name: String): Pair<Obj, Boolean> {
        val obj = store[name]
        return if (obj != null) {
            Pair(obj, true)
        } else {
            outer?.get(name) ?: Pair(NULL, false)
        }
    }

    fun set(name: String, value: Obj): Obj {
        store[name] = value
        return value
    }
}

fun newEnclosedEnvironment(outer: Environment): Environment {
    return Environment(outer = outer)
}