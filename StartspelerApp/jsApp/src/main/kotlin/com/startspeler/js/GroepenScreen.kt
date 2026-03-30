package com.startspeler.js

import com.startspeler.dto.GroupMemberItem
import com.startspeler.dto.GroupOverviewItem
import com.startspeler.ui.GroepenPage
import react.FC
import react.Props
import react.useEffectOnce
import react.useState

val GroepenScreen = FC<Props> {
    val (groups, setGroups) = useState<List<GroupOverviewItem>>(emptyList())
    val (loading, setLoading) = useState(true)
    val (error, setError) = useState<String?>(null)

    useEffectOnce {
        try {
            setGroups(
                listOf(
                    GroupOverviewItem(
                        id = 1,
                        name = "Vrijwilligers",
                        discountPercentage = 10f,
                        memberCount = 3,
                        members = listOf(
                            GroupMemberItem(1, "Jan Peeters"),
                            GroupMemberItem(2, "Lotte Vermeulen"),
                            GroupMemberItem(3, "Tom Janssens")
                        )
                    ),
                    GroupOverviewItem(
                        id = 2,
                        name = "Organisatie",
                        discountPercentage = 15f,
                        memberCount = 2,
                        members = listOf(
                            GroupMemberItem(4, "Sara Claes"),
                            GroupMemberItem(5, "Milan De Smet")
                        )
                    ),
                    GroupOverviewItem(
                        id = 3,
                        name = "Sponsors",
                        discountPercentage = 5f,
                        memberCount = 4,
                        members = listOf(
                            GroupMemberItem(6, "Emma Wouters"),
                            GroupMemberItem(7, "Noah Maes"),
                            GroupMemberItem(8, "Liam Jacobs"),
                            GroupMemberItem(9, "Julie Martens")
                        )
                    )
                )
            )
        } catch (t: Throwable) {
            setError(t.message ?: "Groepen konden niet geladen worden")
        } finally {
            setLoading(false)
        }
    }

    GroepenPage {
        this.groups = groups
        this.loading = loading
        this.error = error
    }
}
