package com.example.senior_on.domain.model.parent

enum class SeniorRelationType(
    val displayLabel: String,
) {
    MOTHER("어머니"),
    FATHER("아버지"),
    GRANDPARENT("조부모"),
    OTHER("직접 작성"),
}

data class CaregiverRelationship(
    val relation: SeniorRelationType,
    val customRelation: String? = null,
) {
    val displayLabel: String
        get() = when (relation) {
            SeniorRelationType.OTHER -> customRelation.orEmpty().trim()
            else -> relation.displayLabel
        }

    companion object {
        fun fromDisplayLabel(label: String): CaregiverRelationship {
            val trimmedLabel = label.trim()
            val relation = SeniorRelationType.entries.firstOrNull { type ->
                type != SeniorRelationType.OTHER &&
                    type.displayLabel == trimmedLabel
            }

            return if (relation != null) {
                CaregiverRelationship(relation = relation)
            } else {
                CaregiverRelationship(
                    relation = SeniorRelationType.OTHER,
                    customRelation = trimmedLabel,
                )
            }
        }
    }
}
