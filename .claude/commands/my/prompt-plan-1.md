Read the attached new_requirements.md and generate or update a prompt_plan.md with TDD-focused
implementation steps. Keep already checked steps as checked if a prompt_plan.md already exists .

For each feature in the spec:
1. First prompt: Write failing tests
2. Second prompt: Implement to make tests pass
3. Third prompt: Refactor if needed

Format each prompt like this:
- [ ] N. [TEST] Write tests for [feature] that verify [expected behavior]
- [ ] N+1. [IMPL] Implement [feature] to pass all tests
- [ ] N+2. [CHECK] Run full test suite and ask user to review changes
- [ ] N+3. [COMMIT] Commit changes with message if user agreed in step above

Include these phases:
- Project setup (dependencies, structure, tooling)
- Core features (one at a time, test-first)
- Integration (putting features together)
- Polish (error handling, edge cases, docs)

Make prompts atomic - each should be completable in one Claude Code session.