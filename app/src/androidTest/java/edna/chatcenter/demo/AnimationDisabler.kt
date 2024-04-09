package edna.chatcenter.demo

import android.os.IBinder
import android.util.Log
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.Arrays

class AnimationDisabler internal constructor() {
    private var originalScaleFactors: FloatArray = FloatArray(0)
    private var setAnimationScalesMethod: Method? = null
    private var getAnimationScalesMethod: Method? = null
    private var windowManagerObject: Any? = null

    init {
        try {
            val windowManagerStubClazz = Class.forName("android.view.IWindowManager\$Stub")
            val asInterface = windowManagerStubClazz.getDeclaredMethod("asInterface", IBinder::class.java)
            val serviceManagerClazz = Class.forName("android.os.ServiceManager")
            val getService = serviceManagerClazz.getDeclaredMethod("getService", String::class.java)
            val windowManagerClazz = Class.forName("android.view.IWindowManager")
            setAnimationScalesMethod = windowManagerClazz.getDeclaredMethod("setAnimationScales", FloatArray::class.java)
            getAnimationScalesMethod = windowManagerClazz.getDeclaredMethod("getAnimationScales")
            val windowManagerBinder = getService.invoke(null, "window") as IBinder
            windowManagerObject = asInterface.invoke(null, windowManagerBinder)
        } catch (e: Exception) {
            throw RuntimeException("Failed to access animation methods", e)
        }
    }

    /**
     * Должен быть вызван внутри [Instrumentation.onStart], перед вызовом super.
     */
    fun disableAnimations() {
        try {
            originalScaleFactors = animationScaleFactors
            setAnimationScaleFactors(DISABLED)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable animations", e)
        }
    }

    /**
     * Должен быть вызван внутри [Instrumentation.onDestroy], перед вызовом super.
     */
    fun enableAnimations() {
        try {
            restoreAnimationScaleFactors()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable animations", e)
        }
    }

    @get:Throws(
        InvocationTargetException::class,
        IllegalAccessException::class
    )
    private val animationScaleFactors: FloatArray
        get() = getAnimationScalesMethod?.invoke(windowManagerObject) as FloatArray

    @Throws(InvocationTargetException::class, IllegalAccessException::class)
    private fun setAnimationScaleFactors(scaleFactor: Float) {
        val scaleFactors = FloatArray(originalScaleFactors.size)
        Arrays.fill(scaleFactors, scaleFactor)
        setAnimationScalesMethod?.invoke(windowManagerObject, *arrayOf<Any>(scaleFactors))
    }

    @Throws(InvocationTargetException::class, IllegalAccessException::class)
    private fun restoreAnimationScaleFactors() {
        setAnimationScalesMethod?.invoke(windowManagerObject, *arrayOf<Any>(originalScaleFactors))
    }

    companion object {
        private val TAG = AnimationDisabler::class.java.simpleName
        private const val DISABLED = 0.0f
    }
}
