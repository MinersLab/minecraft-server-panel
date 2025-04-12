package minerslab.mcsp.util

interface Reference<T> {
    fun getValue(): T

    fun setValue(newValue: T)
}

class ConstantReference<T>(
    private val value: T,
) : Reference<T> {
    override fun getValue() = value

    override fun setValue(newValue: T) = throw UnsupportedOperationException()
}

class MutableReference<T>(
    defaultValue: T,
) : Reference<T> {
    private var value = defaultValue

    override fun setValue(newValue: T) {
        value = newValue
    }

    override fun getValue() = value
}

fun <T> ref(value: T): Reference<T> = ConstantReference(value)

fun <T> mutableRef(defaultValue: T) = MutableReference(defaultValue)
