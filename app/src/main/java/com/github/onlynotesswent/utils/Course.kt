package com.github.onlynotesswent.utils

/**
 * Represents a course that a note belongs to.
 *
 * @param courseCode The code of the course.
 * @param courseName The name of the course.
 * @param courseYear The year of the course.
 * @param publicPath The public path of the course.
 */
data class Course(
    val courseCode: String,
    val courseName: String,
    val courseYear: Int,
    val publicPath: String
) {
  /**
   * Returns the full name of the course, which is the concatenation of the course code and name.
   *
   * @return The full name of the course.
   */
  fun fullName() = "$courseCode: $courseName"

  companion object {
    // default course
    val DEFAULT = Course("DEF-123", "Default Course", 2024, "default")
    // course code max length
    private const val COURSE_CODE_MAX_LENGTH = 10
    // course name max length
    private const val COURSE_NAME_MAX_LENGTH = 50

    private val LOWERCASE_WORDS =
        setOf(
            "a",
            "an",
            "and",
            "as",
            "at",
            "but",
            "by",
            "for",
            "if",
            "in",
            "nor",
            "of",
            "on",
            "or",
            "so",
            "the",
            "to",
            "up",
            "yet")

    /**
     * Formats the course code, only allowing uppercase alphanumeric characters and dashes, and
     * capping length.
     *
     * @param courseCode The course code to format.
     * @return The formatted course code.
     */
    fun formatCourseCode(courseCode: String): String {
      return courseCode
          .filter { it.isLetterOrDigit() || it == '-' }
          .uppercase()
          .take(COURSE_CODE_MAX_LENGTH)
    }

    /**
     * Formats the course name by: Capitalizing Words, capping length, allowing alphanumerics and
     * trimming leading whitespaces.
     *
     * @param courseName The course name to format.
     * @return The formatted course name.
     */
    fun formatCourseName(courseName: String): String {
      return courseName
          .trimStart()
          .replace(Regex("\\s+"), " ")
          .lowercase()
          .split(" ")
          .joinToString(" ") { word ->
            if (word in LOWERCASE_WORDS) word else word.replaceFirstChar { c -> c.uppercase() }
          }
          .take(COURSE_NAME_MAX_LENGTH)
    }
  }
}
