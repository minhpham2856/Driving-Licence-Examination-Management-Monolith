import os
import re

path = r'web/views/examiner/components/candidate-list.jsp'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace status checks
content = content.replace("c.status == 'done'", "c.sectionStatus.name() == 'COMPLETED'")
content = content.replace("c.status == 'awaiting'", "c.sectionStatus.name() == 'AWAITING_SIGNATURE'")
content = content.replace("c.status == 'testing'", "c.sectionStatus.name() == 'IN_PROGRESS'")
content = content.replace("c.status == 'absent'", "c.sectionStatus.name() == 'NOT_STARTED' && false") # Just hack it or remove absent branch. Actually CandidateStatus doesn't have ABSENT.
content = content.replace("c.status == 'suspended'", "c.sectionStatus.name() == 'NOT_STARTED' && false") 

# Replace c.statusLabel
content = content.replace("c.statusLabel", "c.sectionStatus.value")

# Replace c.callEligible
content = content.replace("c.callEligible", "c.sectionStatus.name() == 'NOT_STARTED'")

# Replace c.absent, c.markAbsentEligible, c.suspended, c.awaitingSignature, c.completeEligible
# These blocks were like:
# <c:when test="${c.absent}">
# I will just use regex to remove these blocks or replace their conditions to something that doesn't break JSP.
# Since user said they are legacy UI, I will just remove the buttons entirely.

# The buttons are inside a <td class="examiner-table__actions"> block.
# Let's write a python script that just does basic regex replacements.

