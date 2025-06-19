package com.example.text_resume_generator

data class ResumeResponse(
    val name: String = "",
    val phone: String? = null,
    val email: String? = null,
    val twitter: String? = null,
    val address: String? = null,
    val summary: String? = null,
    val skills: List<String> = emptyList(),
    val projects: List<Project> = emptyList(),
    val experience: List<String>? = null,
    val education: String? = null
)

data class Project(
    val title: String = "",
    val description: String = "",
    val startDate: String? = null,
    val endDate: String? = null
)

