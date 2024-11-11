# Contributing to OnlyNotes

## Table of Contents
- [Issue Naming Conventions](#issue-naming-conventions)
- [Branch Naming Conventions](#branch-naming-conventions)
- [Commit Message Guidelines](#commit-message-guidelines)
- [Pull Request Process](#pull-request-process)
- [Code Style Guidelines](#code-style-guidelines)
- [Testing](#testing)

## Issue Naming Conventions
Issues should be opened only for changes in code/documentation. Figma, Github settings, and other non-commitable changes should be left as drafts. 
They should follow this structure : `Type: Short description`
The **Type** should be one of the following:
| Type       | Purpose                                                         |
|------------|-----------------------------------------------------------------|
| `<Epic>`   | If a feature, the epic this feature belongs to, from [this list](https://github.com/orgs/onlynotes-swent/projects/1/settings/fields/137941145)  |
| `Bugfix`   | Bug fix                                                         |
| `Docs`     | Documentation-only changes                                      |
| `Style`    | Code style changes (formatting, missing semi-colons, etc.)      |

## Branch Naming Conventions
Branches should follow [this](https://medium.com/@abhay.pixolo/naming-conventions-for-git-branches-a-cheatsheet-8549feca2534) structure: `type/short-description`. 
The **type** should be one of the following:
| Type       | Purpose                                                         |
|------------|-----------------------------------------------------------------|
| `feature`  | New feature or enhancement                                      |
| `bugfix`   | Bug fix                                                         |
| `docs`     | Documentation-only changes                                      |
| `style`    | Code style changes (formatting, missing semi-colons, etc.)      |

## Commit Message Guidelines
Commit messages should follow the SWENT [Conventional Commits](https://github.com/swent-epfl/bootcamp-f24-Roshan-y/blob/main/docs/Theory.md#2-commit-messages) specification.
Here is a list of the **type** values that can be used:
| Type       | Purpose                                                         |
|------------|-----------------------------------------------------------------|
| `feat`     | New feature or enhancement                                      |
| `fix`      | Bug fix                                                         |
| `docs`     | Documentation-only changes                                      |
| `style`    | Code style changes (formatting, missing semi-colons, etc.)      |
| `refactor` | Code refactoring without adding new features or fixing bugs     |
| `test`     | Adding or updating tests                                        |
| `chore`    | Other tasks like build process updates, dependency management   |

## Pull Request Process
1. **Always create a new branch**:
    - Never commit or push directly to the `main` branch. All changes must be made on a separate branch created from `main`.
    - Follow the [Branch Naming Conventions](#branch-naming-conventions) outlined above.
    - To create a new branch:
      ```bash
      git checkout -b feature/your-feature-name
      ```
      or use the GitHub UI to create a new branch.

2. **Submit a pull request (PR)**:
    - After making your changes, push your branch and open a pull request (you can do this on GitHub website) to merge your branch into `main`. [The template](.github/pull_request_template.md) we use for pull requests is inspired from [here](https://axolo.co/blog/p/part-3-github-pull-request-template)
    - Ensure your pull request (PR) title follows: `type: short description`
        - **type** and **short description** should be the same as the branch from which this PR was created.
        - E.g. : From `type/short-description` branch, create `type: short description` pull request.
    - Ensure your pull request (PR) description explains:
        - What issue/feature your PR addresses.
        - How to test your changes.
        - Any dependencies required.

3. **Before submitting your pull request**:
    - [ ] Run all tests and ensure they pass.
    - [ ] Ensure your code follows the style guidelines (see [Code Style Guidelines](#code-style-guidelines)).
    - [ ] Ensure all new features are documented.

**Note**: Direct commits/pushes to the `main` branch are **not allowed**. All changes must go through a pull request review and be approved by at least one other team member.

## Code Style Guidelines
_Run Code > Optimize imports_ and _Code > Reformat code_ on all your files, as well as _Code > Inspect code_ to improve the quality of your code. 
Remember to apply `ktfmt` format to your code before committing. You can do this by running it from the gradle. Please comment your code properly and use meaningful variable names. 
View the following [Naming Conventions.md](docs/NamingConventions) to see the naming conventions for packages, classes, functions, properties, and more in Kotlin for this project.
If you want more information on the official Kotlin coding conventions you can visit [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).

## Testing

`Have to Update`
