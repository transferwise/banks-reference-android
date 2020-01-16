/*
 * Copyright 2019 TransferWise Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.banks.demo.offline

/**
 * List of prerecorded backend responses, used by the MockBanksWebServer to allow an offline mode.
 *
 * ⚠️ This is only part of the repository for demo purposes, but has no value as reference code. ⚠️
 */
internal const val FAKE_QUOTE = """
            {
                "id": "53eed2b1-ac01-4e33-a500-eca21c3e0629",
                "sourceCurrency": "GBP",
                "targetCurrency": "EUR",
                "sourceAmount": 100.0,
                "targetAmount": 118.73,
                "rate": 1.19487,
                "fee": 0.63,
                "formattedEstimatedDelivery": "by December 16th"
            }
        """

internal const val FAKE_CUSTOMER = """
            {
                "id": 8,
                "firstName": "Bank",
                "lastName": "Customer 4",
                "dateOfBirth": "1980-01-01",
                "phoneNumber": "+447777788222",
                "email": "bank-customer-4@bank.com",
                "transferWiseAccountLinked": true
            }
        """

internal const val FAKE_ANONYMOUS_QUOTE = """
            {
                "id": null,
                "sourceCurrency": "GBP",
                "targetCurrency": "EUR",
                "sourceAmount": 1000.0,
                "targetAmount": 1190.15,
                "rate": 1.19487,
                "fee": 3.95,
                "formattedEstimatedDelivery": "by December 16th"
            }
        """

internal const val FAKE_RECIPIENTS_REQUIEMENTS = """
            [
                {
                    "type": "sweden_local",
                    "title": "Local bank account",
                    "fields": [
                        {
                            "name": "Recipient type",
                            "group": [
                                {
                                    "key": "legalType",
                                    "name": "Recipient type",
                                    "type": "select",
                                    "refreshRequirementsOnChange": false,
                                    "required": true,
                                    "displayFormat": null,
                                    "example": "",
                                    "minLength": null,
                                    "maxLength": null,
                                    "validationRegexp": null,
                                    "validationAsync": null,
                                    "valuesAllowed": [
                                        {
                                            "key": "PRIVATE",
                                            "name": "Person"
                                        },
                                        {
                                            "key": "BUSINESS",
                                            "name": "Business"
                                        }
                                    ]
                                }
                            ]
                        },
                        {
                            "name": "Clearing number",
                            "group": [
                                {
                                    "key": "clearingNumber",
                                    "name": "Clearing number",
                                    "type": "text",
                                    "refreshRequirementsOnChange": false,
                                    "required": true,
                                    "displayFormat": null,
                                    "example": "1234",
                                    "minLength": 4,
                                    "maxLength": 5,
                                    "validationRegexp": null,
                                    "validationAsync": null,
                                    "valuesAllowed": null
                                }
                            ]
                        },
                        {
                            "name": "Account number",
                            "group": [
                                {
                                    "key": "accountNumber",
                                    "name": "Account number",
                                    "type": "text",
                                    "refreshRequirementsOnChange": false,
                                    "required": true,
                                    "displayFormat": null,
                                    "example": "1234567",
                                    "minLength": 1,
                                    "maxLength": 10,
                                    "validationRegexp": null,
                                    "validationAsync": null,
                                    "valuesAllowed": null
                                }
                            ]
                        }
                    ]
                },
                {
                    "type": "bankgiro",
                    "title": "Bankgiro account",
                    "fields": [
                        {
                            "name": "Bankgiro number",
                            "group": [
                                {
                                    "key": "bankgiroNumber",
                                    "name": "Bankgiro number",
                                    "type": "text",
                                    "refreshRequirementsOnChange": false,
                                    "required": true,
                                    "displayFormat": "***-****||****-****",
                                    "example": "1234-5678",
                                    "minLength": 7,
                                    "maxLength": 9,
                                    "validationRegexp": "^\\d{3,4}\\-?\\d{4}${'$'}",
                                    "validationAsync": null,
                                    "valuesAllowed": null
                                }
                            ]
                        }
                    ]
                },
                {
                    "type": "iban",
                    "title": "IBAN",
                    "fields": [
                        {
                            "name": "Recipient type",
                            "group": [
                                {
                                    "key": "legalType",
                                    "name": "Recipient type",
                                    "type": "select",
                                    "refreshRequirementsOnChange": false,
                                    "required": true,
                                    "displayFormat": null,
                                    "example": "",
                                    "minLength": null,
                                    "maxLength": null,
                                    "validationRegexp": null,
                                    "validationAsync": null,
                                    "valuesAllowed": [
                                        {
                                            "key": "PRIVATE",
                                            "name": "Person"
                                        },
                                        {
                                            "key": "BUSINESS",
                                            "name": "Business"
                                        }
                                    ]
                                }
                            ]
                        },
                        {
                            "name": "Bank code (BIC/SWIFT)",
                            "group": [
                                {
                                    "key": "BIC",
                                    "name": "Bank code (BIC/SWIFT)",
                                    "type": "text",
                                    "refreshRequirementsOnChange": false,
                                    "required": false,
                                    "displayFormat": null,
                                    "example": "Choose recipient bank",
                                    "minLength": null,
                                    "maxLength": null,
                                    "validationRegexp": "^[A-Za-z]{6}[A-Za-z\\d]{2}([A-Za-z\\d]{3})?${'$'}",
                                    "validationAsync": null,
                                    "valuesAllowed": null
                                }
                            ]
                        },
                        {
                            "name": "IBAN",
                            "group": [
                                {
                                    "key": "IBAN",
                                    "name": "IBAN",
                                    "type": "text",
                                    "refreshRequirementsOnChange": false,
                                    "required": true,
                                    "displayFormat": "**** **** **** **** **** **** **** ****",
                                    "example": "DE89370400440532013000",
                                    "minLength": 2,
                                    "maxLength": null,
                                    "validationRegexp": null,
                                    "validationAsync": null,
                                    "valuesAllowed": null
                                }
                            ]
                        }
                    ]
                }
            ]
        """

internal const val FAKE_RECIPIENTS = """
            [
                {
                    "id": 13709553,
                    "currency": "SEK",
                    "active": true,
                    "ownedByCustomer": false,
                    "type": "Iban",
                    "accountSummary": "NL29 INGB 7673 6570 82",
                    "name": {
                        "fullName": "Adan Blair"
                    }
                },
                {
                    "id": 13709370,
                    "currency": "SEK",
                    "active": true,
                    "ownedByCustomer": false,
                    "type": "Iban",
                    "accountSummary": "BE12 9788 3336 2792",
                    "name": {
                        "fullName": "Ade Lee"
                    }
                },
                {
                    "id": 13709349,
                    "currency": "SEK",
                    "active": true,
                    "ownedByCustomer": false,
                    "type": "Iban",
                    "accountSummary": "FI94 5625 7499 4861 47",
                    "name": {
                        "fullName": "Adrian Livingston"
                    }
                },
                {
                    "id": 13709347,
                    "currency": "SEK",
                    "active": true,
                    "ownedByCustomer": false,
                    "type": "Iban",
                    "accountSummary": "CY18 7842 8614 1385 3266 2968 8219",
                    "name": {
                        "fullName": "Alan Macandrew"
                    }
                },
                {
                    "id": 13709340,
                    "currency": "SEK",
                    "active": true,
                    "ownedByCustomer": false,
                    "type": "Iban",
                    "accountSummary": "AD095 85433 2998 6851 7191 95",
                    "name": {
                        "fullName": "April Rimmington"
                    }
                },
                {
                    "id": 13708906,
                    "currency": "SEK",
                    "active": true,
                    "ownedByCustomer": false,
                    "type": "Iban",
                    "accountSummary": "SE74 3918 4314 1263 7135 7296",
                    "name": {
                        "fullName": "Julia Wong"
                    }
                },
                {
                    "id": 13708903,
                    "currency": "SEK",
                    "active": true,
                    "ownedByCustomer": false,
                    "type": "Iban",
                    "accountSummary": "BE70 1311 1662 9125",
                    "name": {
                        "fullName": "Jeroen Mols"
                    }
                },
                {
                    "id": 13708904,
                    "currency": "SEK",
                    "active": true,
                    "ownedByCustomer": false,
                    "type": "Iban",
                    "accountSummary": "SE09 3224 5163 3164 9129 4733",
                    "name": {
                        "fullName": "Sarah Toon"
                    }
                },
                {
                    "id": 13708226,
                    "currency": "GBP",
                    "active": true,
                    "ownedByCustomer": true,
                    "type": "SortCode",
                    "accountSummary": "(23-14-70) 28821822",
                    "name": {
                        "fullName": "Will Davies"
                    }
                }
            ]
        """

internal const val FAKE_CURRENCIES = """
            [
                {
                    "code": "GBP",
                    "name": "Pound Sterling",
                    "mostPopular": true,
                    "countries": [
                      "UK"
                    ]
                },
                {
                    "code": "EUR",
                    "name": "Euro",
                    "mostPopular": true,
                    "countries": [
                      "Belgium",
                      "Portugal"
                    ]
                },
                {
                    "code": "USD",
                    "name": "United States Dollar",
                    "mostPopular": false,
                    "countries": [
                      "United States of America"
                    ]
                }
            ]
        """

internal const val FAKE_TRANSFER_REQUIREMENTS = """
            [
                {
                    "type": "transfer",
                    "usageInfo": null,
                    "fields": [
                        {
                            "name": "Transfer reference",
                            "group": [
                                {
                                    "key": "reference",
                                    "name": "Transfer reference",
                                    "type": "text",
                                    "refreshRequirementsOnChange": false,
                                    "required": false,
                                    "displayFormat": null,
                                    "example": null,
                                    "minLength": null,
                                    "maxLength": 20,
                                    "validationRegexp": null,
                                    "validationAsync": null,
                                    "valuesAllowed": null
                                }
                            ]
                        },
                        {
                            "name": "Transfer purpose",
                            "group": [
                                {
                                    "key": "transferPurpose",
                                    "name": "Transfer purpose",
                                    "type": "select",
                                    "refreshRequirementsOnChange": false,
                                    "required": true,
                                    "displayFormat": null,
                                    "example": null,
                                    "minLength": null,
                                    "maxLength": null,
                                    "validationRegexp": null,
                                    "validationAsync": null,
                                    "valuesAllowed": [
                                        {
                                            "key": "CONTRIBUTING_TO_PERSONAL_SAVINGS",
                                            "name": "Savings"
                                        },
                                        {
                                            "key": "GENERAL_MONTHLY_LIVING_EXPENSES",
                                            "name": "Living expenses"
                                        },
                                        {
                                            "key": "INVESTING_IN_FUNDS_STOCKS_BONDS_OPTIONS_FUTURES_OR_OTHER",
                                            "name": "Investing"
                                        },
                                        {
                                            "key": "PAYING_FOR_GOODS_OR_SERVICES_ABROAD",
                                            "name": "Goods and services"
                                        },
                                        {
                                            "key": "PAYING_RENT_MORTGAGE_BANK_LOAN_INSURANCE_CREDIT",
                                            "name": "Mortgage or loan"
                                        },
                                        {
                                            "key": "PAYING_RENT_UTILITIES_OR_PROPERTY_CHARGES",
                                            "name": "Bills"
                                        },
                                        {
                                            "key": "RECEIVE_SALARY_IN_DIFFERENT_CURRENCY",
                                            "name": "Salary"
                                        },
                                        {
                                            "key": "RECEIVE_PENSION_IN_DIFFERENT_CURRENCY",
                                            "name": "Pension"
                                        },
                                        {
                                            "key": "SENDING_MONEY_REGULARLY_TO_FAMILY",
                                            "name": "Family support"
                                        },
                                        {
                                            "key": "SENDING_MONEY_TO_MY_OWN_ACCOUNT_TO_BENEFIT_FROM_EXHCANGE_RATE",
                                            "name": "Hedging"
                                        }
                                    ]
                                }
                            ]
                        }
                    ]
                }
            ]
        """

internal const val FAKE_TRANSFER_SUMMARY = """
            {
                "quoteId": "28658b71-8508-4853-b7bc-d777b0aaaa00",
                "recipientId": 13732254,
                "sourceCurrency": "GBP",
                "targetCurrency": "SEK",
                "sourceAmount": 100.0,
                "targetAmount": 1233.56,
                "rate": 12.4326,
                "fee": 0.78,
                "recipientName": "Mr. Squarepants",
                "accountSummary": "SE84 9172 2312 5197 9197 2297",
                "formattedEstimatedDelivery": "by November 18th",
                "transferReferenceValidation": {
                    "maxLength": 20,
                    "minLength": 5,
                    "validationRegexp": "^[a-z|A-Z]*${'$'}"
                }
            }
        """
