package im.threads.business.annotation

@Target(AnnotationTarget.ANNOTATION_CLASS)
annotation class OpenClass

@OpenClass
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class OpenForTesting
