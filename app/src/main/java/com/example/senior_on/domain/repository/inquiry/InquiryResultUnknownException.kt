package com.example.senior_on.domain.repository.inquiry

/** The create request may have been stored even though no usable confirmation arrived. */
class InquiryResultUnknownException(cause: Throwable) : Exception(cause)
