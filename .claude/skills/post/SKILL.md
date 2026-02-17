---
name: post
description: Generate authentic social media post from recent development work. Uses 4-step pipeline with fact collection, writer agent, slop gate, and human review. Target platform is X (Twitter) in English.
allowed-tools: Task, AskUserQuestion, Read
argument-hint: [optional: topic or "auto" to analyze recent work]
---

# post: Generate Social Media Content

This skill creates authentic X (Twitter) posts about development work using a 4-step quality pipeline.

## Usage

```bash
# Analyze recent conversation for post-worthy content
/post

# Generate post about specific topic
/post "Created 5 custom skills for Kotlin workflow"

# Auto-analyze and suggest topics
/post auto
```

## The 4-Step Pipeline

Follow the pipeline defined in `.claude/rules/Social.md`:

### Step 1: Fact Collection

**CRITICAL: Without concrete facts, STOP and ask the user.**

Scan the recent conversation (last 50-100 messages) for:
- ✅ Numbers (metrics, LOC, time, percentages)
- ✅ Tools (technologies, libraries, versions)
- ✅ Results (outcomes, changes, improvements)
- ✅ User's actual phrases/quotes
- ✅ Context (problem solved, why it matters)

**If insufficient facts:**
```
I need more concrete facts to write an authentic post. Can you provide:

1. Specific numbers/metrics?
2. Tools/technologies used?
3. Measurable results/outcomes?
4. What problem this solved?

Without facts, the post will be generic AI-slop.
```

**Output from this step:**
```
Facts collected:
- [fact 1 with source]
- [fact 2 with source]
- [fact 3 with source]
- User quote: "[exact quote]"
```

### Step 2: Writer Agent

Use **Task tool with general-purpose agent** to write the post.

**Prompt for writer agent:**
```
Write a post for X (Twitter) in English about this development work.

FACTS (use ONLY these, don't make up anything):
[paste facts from Step 1]

VOICE GUIDELINES:
- First person (I/we)
- Short sentences
- Lead with most interesting fact
- Specific numbers and tools
- Show don't tell (facts over adjectives)
- Casual, conversational
- Honest about failures
- No marketing language
- Max 280 characters (single tweet) or thread if needed

BANNED WORDS: See .claude/rules/Social.md Slop Gate section

Main rule: If it's not in the FACTS, don't write it.

Output format:
[post text]
```

**Get the draft from the writer agent.**

### Step 3: Slop Gate Validation

**Critical check against banned phrases and AI patterns.**

Load the slop gate rules from `.claude/rules/Social.md`:
1. Check against 67 banned phrases
2. Check against Wikipedia AI patterns
3. Check for generic statements that could apply to anything
4. Check for marketing/influencer language

**For each violation found:**
```
❌ SLOP DETECTED: "[phrase]"
Line: "[sentence containing it]"
Pattern: [banned phrase / AI pattern / generic puffery]
```

**If violations found:**
- Count total violations
- If > 3: Send back to writer agent with feedback
- If 1-3: Mark as warnings, let user decide
- If 0: ✅ PASSED

**Slop gate output:**
```
Slop Gate Results:
✅ PASSED - No AI-slop detected
or
⚠️ WARNINGS - 2 minor issues:
  - Line 1: "However" (transition word overuse)
  - Line 3: Possible generic statement
or
❌ FAILED - 5 violations found:
  - [list violations]
  Sending back to writer for revision...
```

### Step 4: Human Review

Present the final draft with all context:

```markdown
## 📝 Draft Post Ready for Review

**Post:**
[final post text - copy-ready]

**Stats:**
- Characters: X/280
- Thread: [Yes (N tweets) / No]
- Reading time: ~X seconds

**Facts Used:**
[bulleted list of facts from conversation]

**Slop Gate:** [✅ PASSED / ⚠️ WARNINGS / ❌ FAILED]
[If warnings/failed: list issues]

**Source conversation:**
- Based on messages from [time range]
- Key topics: [list topics]

---

**Your options:**
1. ✅ **Approve** - Ready to post
2. ✏️ **Edit** - Make changes (I'll learn from them)
3. ❌ **Reject** - Not posting this (tell me why)
4. 🔄 **Revise** - Give me specific feedback to regenerate

What would you like to do?
```

**Handle user response:**
- Approve → Done
- Edit → Log changes for learning
- Reject → Log reason, learn from it
- Revise → Take feedback, go back to Step 2

## Examples

### Example 1: Feature Development Post

**User invokes:** `/post`

**Step 1 - Facts collected:**
```
- Created 5 custom Claude Code skills
- Skills: ktlint-fix, full-check, new-feature, compose-screen, spring-endpoint
- Each skill took ~15-20 minutes to create
- Enabled ktlint trailing comma rules
- Added named arguments guideline (3+ params)
- Project: Spring Boot + Compose Multiplatform
- Follows 200-line iteration rule
- User removed spring-endpoint (not competent to review)
```

**Step 2 - Writer output:**
```
Built 5 custom Claude Code skills for my Kotlin workflow today:

/ktlint-fix - auto-format + verify
/full-check - run all CI checks locally
/new-feature - scaffolds iteration plan (200 LOC max)
/compose-screen - ViewModel + UI state boilerplate

Each skill took ~15min to write. Now my entire CI pipeline runs in one command.
```

**Step 3 - Slop gate:**
```
✅ PASSED
- No banned phrases detected
- No AI patterns found
- Concrete facts present
- Authentic voice maintained
```

**Step 4 - Human review:**
```
## 📝 Draft Post Ready for Review

**Post:**
Built 5 custom Claude Code skills for my Kotlin workflow today:

/ktlint-fix - auto-format + verify
/full-check - run all CI checks locally
/new-feature - scaffolds iteration plan (200 LOC max)
/compose-screen - ViewModel + UI state boilerplate

Each skill took ~15min to write. Now my entire CI pipeline runs in one command.

**Stats:**
- Characters: 274/280
- Thread: No
- Reading time: ~12 seconds

**Facts Used:**
- 5 custom skills created
- Specific skill names and purposes
- 200 LOC iteration constraint
- Time metric: 15 min per skill
- Benefit: CI pipeline in one command

**Slop Gate:** ✅ PASSED

**Your options:**
1. ✅ Approve - Ready to post
2. ✏️ Edit - Make changes
3. ❌ Reject - Not posting
4. 🔄 Revise - Give feedback

What would you like to do?
```

### Example 2: Technical Learning Post

**User invokes:** `/post "Learned about ktlint trailing comma rules"`

**Facts needed - ASK USER:**
```
I need more concrete facts about the ktlint trailing comma rules. Can you provide:

1. What specific rules did you enable?
2. Why did you enable them?
3. What problem does this solve?
4. Any metrics (files affected, issues found, etc.)?

Without these facts, I can only write generic AI-slop.
```

## Error Handling

**Insufficient facts:**
- STOP and ask user for specific details
- List what facts are missing
- Explain why they're needed

**Writer agent fails:**
- Log the error
- Ask user if they want to try again or provide different facts
- Don't present a bad draft

**Slop gate fails (>5 violations):**
- Don't present to user
- Send back to writer with violations list
- Max 3 revision attempts, then ask user for help

**User rejects repeatedly:**
- Log patterns in rejections
- Ask for feedback: "What should I change about how I write posts?"
- Update approach based on feedback

## Learning & Improvement

After each post (approved or rejected):

1. **Log the outcome:**
   - Facts used
   - Final text (if approved)
   - User edits (if edited)
   - Rejection reason (if rejected)

2. **Identify patterns:**
   - What facts lead to approved posts?
   - What language gets edited out?
   - What topics work best?

3. **Adjust approach:**
   - Update fact collection priorities
   - Refine voice guidelines
   - Add to slop gate if new AI patterns emerge

4. **Ask for meta-feedback periodically:**
   - "How's the post quality been?"
   - "Should I adjust the tone/length/style?"
   - "Any new topics you want to focus on?"

## Integration with Development Workflow

This skill can be triggered:
1. **Manually** by user: `/post` when they want to share something
2. **Auto-suggested** after iterations (per Social.md rule)
3. **Scheduled** (e.g., "Friday recap" posts)

The pipeline ensures quality while being fast enough not to interrupt the development flow.
