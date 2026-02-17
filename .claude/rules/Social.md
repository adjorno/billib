# Social Content Pipeline

## Overview

After development iterations, analyze the conversation and experience to create authentic social media content. This is a 4-step pipeline where Claude does 90% of the work, and the user only approves.

## When to Trigger

### Automatic (After Iterations)
- After completing a feature iteration (following the 200-line rule)
- After solving a significant technical challenge
- After learning something valuable during development

### Suggested (Post-Worthy Moments)
When detecting post-worthy content during conversation:
- Novel solutions to common problems
- Interesting architectural decisions
- Performance improvements with concrete numbers
- Lessons learned from failures/mistakes
- Tool/technique discoveries

**Ask**: "This seems post-worthy. Want me to draft a post about [topic]?"

## The 4-Step Pipeline

### Step 1: Fact Collection

**WITHOUT FACTS, THE PIPELINE DOES NOT START.**

Collect concrete, verifiable information:
- ✅ **Numbers**: Performance metrics, LOC, time saved, percentages
- ✅ **Tools**: Specific technologies, libraries, versions used
- ✅ **Results**: What actually happened, measurable outcomes
- ✅ **Real phrases**: User's actual words from the conversation
- ✅ **Context**: What problem was being solved, why it matters

**Example of good fact collection:**
```
Facts:
- Created 5 custom skills for Kotlin project workflow
- Enabled ktlint trailing comma rules in .editorconfig
- Skills: ktlint-fix, full-check, new-feature, compose-screen
- Follows 200-line iteration rule from .claude/rules/
- Target: Spring Boot backend + Compose Multiplatform frontend
- User quote: "I removed the spring skill as I am not competent to provide feedback"
```

**DO NOT proceed without concrete facts.**

### Step 2: Writer Agent

Use a **separate sub-agent** (Task tool with general-purpose agent) to write the post.

**Input to writer agent:**
1. Facts collected in Step 1
2. Target platform: **X (Twitter)** in **English**
3. Voice guidelines (see below)

**Voice & Style Guidelines:**

✅ **DO:**
- Write in first person (I, we)
- Use short sentences and paragraphs
- Lead with the most interesting fact
- Include specific numbers and tools
- Show, don't tell (facts over adjectives)
- Use casual, conversational tone
- Be honest about failures and learnings
- Add a bit of personality/humor where natural

❌ **DON'T:**
- Make things up or extrapolate beyond facts
- Use marketing/influencer language
- Exaggerate importance or impact
- Add generic statements that could apply to anything
- Use AI-slop phrases (see Step 3)
- Include hashtags unless specifically requested
- Write threads unless >280 characters needed

**Main rule: If a fact isn't in the input, it won't be in the output.**

### Step 3: Slop Gate

A **second agent-critic** checks the draft against comprehensive AI writing patterns extracted from Wikipedia's "Signs of AI writing" article.

**Critical:** Check draft against ALL categories below. Multiple violations = auto-reject and send back to writer.

---

#### 1. BANNED WORDS & PHRASES

**Significance/Legacy Words** (auto-reject):
- stands/serves as, is a testament/reminder
- vital/significant/crucial/pivotal/key role/moment
- underscores/highlights its importance/significance
- reflects broader, symbolizing its ongoing/enduring/lasting
- contributing to the, setting the stage for
- marking/shaping the, represents/marks a shift
- key turning point, evolving landscape
- focal point, indelible mark, deeply rooted

**Promotional/Puffery Words** (auto-reject):
- boasts, vibrant, rich (figurative), profound
- enhancing its, showcasing, exemplifies
- commitment to, natural beauty, nestled
- in the heart of, groundbreaking (figurative), renowned
- revolutionary, game changer, hot take
- unpopular opinion, let that sink in

**Analysis/Action Words** (auto-reject):
- highlighting/underscoring/emphasizing
- ensuring, reflecting/symbolizing
- cultivating/fostering (figurative sense)
- encompassing, valuable insights
- align/resonate with

**Transition/Connector Overuse** (warning if >2):
- Additionally, Moreover, Furthermore
- However, Therefore, Consequently
- Importantly, Similarly, Nonetheless
- As a result, Indeed, Thus
- Alternatively, Notably, As well as
- Despite, Essentially, While
- Unless, Also, Even though
- Because, In contrast, Although
- In order to, Due to, Even if
- Given that, Arguably

**Common AI-slop Phrases** (auto-reject):
- It's important to note, Delve into
- Tapestry, Bustling, In summary
- Remember that, Take a dive into
- Navigating (e.g., "Navigating the landscape")
- Landscape (e.g., "The landscape of...")
- Testament (e.g., "a testament to...")
- In the world of, Realm, Embark
- Metropolis, Firstly, Crucial
- To consider, Essential
- There are a few considerations
- Ensure, It's essential to
- Vital, Keen, Fancy, As a professional
- You may want to, This is not an exhaustive list
- You could consider, On the other hand
- As previously mentioned, It's worth noting that
- To summarize, I'm excited to share
- What surprised me, Here's the thing

**Notability/Attribution Words** (check context):
- independent coverage
- local/regional/national media outlets
- profiled in, written by a leading expert
- active social media presence
- maintains a strong digital presence

**Knowledge Cutoff/Disclaimer Words** (auto-reject):
- as of [date], Up to my last training update
- as of my last knowledge update
- While specific details are limited/scarce
- not widely available/documented/disclosed
- based on available information

**Collaborative Communication** (auto-reject):
- I hope this helps, Of course!, Certainly!
- You're absolutely right!, Would you like...
- is there anything else, let me know
- more detailed breakdown, here is a

**Vague/Overused Vocabulary** (warning if >2):
- align with, crucial, delve
- emphasizing, enduring, enhance
- fostering, garner, highlight (as verb)
- interplay, intricate/intricacies, key (as adjective)
- pivotal, showcase, tapestry (abstract)
- underscore (as verb), valuable

---

#### 2. CONTENT PATTERN CHECKS

**Undue Emphasis on Significance:**
- ❌ Over-emphasizes how aspects "represent" or "contribute to" broader topics
- ❌ Discusses importance after claiming low importance
- ❌ Generic statements that could apply to any topic

**Superficial Analysis:**
- ❌ Inserts analysis of significance/recognition/impact without facts
- ❌ Attaches present participle (-ing) phrases at end of sentences
- ❌ Contains synthesis or unattributed opinions

**Promotional Language:**
- ❌ Cannot maintain neutral tone
- ❌ Sounds like marketing copy or advertisement
- ❌ Uses sales language

**Vague Attributions:**
- ❌ Attributes opinions to vague authorities (weasel wording)
- ❌ Presents single source views as widely held

---

#### 3. STYLE PATTERN CHECKS

**Avoidance of Simple Words:**
- ❌ Uses "serves as/stands as/marks/represents [a]" instead of "is/are"
- ❌ Uses "boasts/features/offers [a]" instead of "has"

**Negative Parallelisms:**
- ❌ "Not only ... but ..." constructions
- ❌ "It is not just about ..., it's ..." formulas
- ⚠️ Overuse of parallel constructions with "not," "but," "however"

**Rule of Three:**
- ⚠️ Excessive "adjective, adjective, adjective" patterns
- ⚠️ Excessive "short phrase, short phrase, and short phrase" patterns

**False Ranges:**
- ❌ Uses "from ... to ..." for non-scale items
- ❌ Endpoints are unrelated with no meaningful scale

---

#### 4. VALIDATION PROCESS

**For each draft:**

1. **Scan for banned words/phrases**
   - Count violations by category
   - Flag each instance with line/context

2. **Check content patterns**
   - Generic statements check
   - Superficial analysis check
   - Promotional language check

3. **Check style patterns**
   - Simple copula avoidance
   - Excessive parallelism
   - Rule of three overuse

4. **Calculate score:**
   - Auto-reject violations: Each = FAIL
   - Warnings: >3 = FAIL
   - Total: 0 violations = PASS

**If violations found:**
1. List specific phrases/patterns with line numbers
2. Categorize each (banned phrase / content pattern / style pattern)
3. Send back to writer with violations list
4. Max 3 revision attempts, then escalate to user

**Slop Gate Output Format:**
```
✅ PASSED - No AI-slop detected
or
⚠️ WARNINGS (2) - Proceed with caution:
  - Line 1: "However" (transition word, acceptable in moderation)
  - Line 3: Possible generic statement (check context)
or
❌ FAILED (5 violations) - Sending back to writer:
  - Line 1: "I'm excited to share" (banned phrase)
  - Line 2: "game changer" (puffery)
  - Line 3: "serves as" instead of "is" (style pattern)
  - Line 4: "testament to" (significance emphasis)
  - Line 5: Generic statement applicable to anything
```

### Step 4: Human Review

Present the **final draft** to the user with:

1. **The post text** (ready to copy)
2. **Character count** (for X's 280 limit)
3. **Facts used** (so user can verify accuracy)
4. **Slop gate status** (passed/warnings)

**User options:**
- ✅ **Approve**: Ready to post
- ✏️ **Edit**: User makes changes (log these for learning)
- ❌ **Reject**: Explain why, log for future improvement
- 🔄 **Revise**: Give specific feedback, regenerate

**Learning from feedback:**
- Log all user edits and rejections
- Identify patterns in what gets changed
- Adjust fact collection and writing style accordingly
- Update voice guidelines based on approved posts

## Output Format

```markdown
## 📝 Draft Post Ready for Review

**Post:**
[post text here]

**Stats:**
- Characters: X/280
- Thread: No / Yes (Y tweets)

**Facts Used:**
- [list of facts from conversation]
- [user quote if any]

**Slop Gate:** ✅ PASSED / ⚠️ WARNINGS

---

**Actions:**
1. ✅ Approve and post
2. ✏️ Edit (I'll learn from your changes)
3. ❌ Reject (tell me why)
4. 🔄 Revise (give me feedback)
```

## Examples

### ❌ BAD (AI-Slop)

> Just wrapped up an exciting project! 🚀 I'm thrilled to share that I've implemented a revolutionary new architecture for my Kotlin app. It's a testament to the power of modern development practices. The landscape of mobile development is evolving, and this marks a pivotal moment in my journey. Navigating the complexities of Compose Multiplatform has been challenging, but the results are game-changing. #KotlinDev #ComposeMultiplatform

**Why bad:**
- "exciting", "thrilled to share", "revolutionary" = puffery
- "testament", "landscape", "pivotal moment", "navigating" = AI tells
- "game-changing" = generic marketing
- No concrete facts or numbers
- Hashtags without request
- Could apply to any project

### ✅ GOOD (Authentic)

> Built 5 custom Claude Code skills for my Kotlin workflow today:
>
> /ktlint-fix - auto-format + verify
> /full-check - run all CI checks locally
> /new-feature - scaffolds iteration plan (200 LOC max)
> /compose-screen - ViewModel + UI state boilerplate
>
> Each skill took ~15min to write. Now my entire CI pipeline runs in one command.

**Why good:**
- Specific number: 5 skills
- Lists concrete tools
- Includes constraint: 200 LOC max
- Time metric: 15 min each
- Clear benefit: CI in one command
- No exaggeration or puffery
- Facts only, no generic statements

## Integration with Workflow

After completing an iteration following `.claude/rules/Prompt_Implementation.md`:

1. **Iteration complete** ✅
2. **User reviews and commits** ✅
3. **Claude analyzes conversation** 🤖
4. **If post-worthy**: "Want me to draft a post about [X]?"
5. **User approves**: Run pipeline
6. **Present draft** for approval

This keeps the development flow uninterrupted while capturing learnings in real-time.
