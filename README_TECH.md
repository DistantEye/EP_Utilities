# EP_Utilities Technical Description

This document goes into some of the more techical explanations about the project and it's structure.

## Intent

Some aspects of this project are fairly generalized, perhaps overly so, as well as some parent/child structures where the parent is never used in the project. This is because of an intent to eventually release a System-agnostic version of the project.

The code can't currently support such a thing particularly easily, but as many steps are being taken as possible to leave that road minimally obstructed down the line.

## Object Hierachy

Here lies a general breakdown of the major classes and functions

### Containers package

All the encapsulations of complex data types used by the program, from Character to Skill to Trait. These classes have many helper methods to manage interactions with the UI or each other, but are meant to have minimal operating functions.

### UI Classes

All of our front end bits.

* UISkeleton : Code common enough to be useful across multiple children.

* LifePathUI : Front end for the LifePath Generation Process

* CharacterSheetUI : General front end for displaying, creating, and modifying characters. Can load results from the other UIs/processes

* PackageGenUI (Planned, not implemented) : Front end for Character creation via package buying

### EpEnvironmment

EpEnvironment is the back end driver of any action in LifePathUI or PackageGenUI. The environment operates in a sequential fashion, starting on a predefined Step, advancing each time the UI instructs, according to the following workflow:

1. Read in Step's list of Commands

2. Split list of Commands and loop for each one

  1. If Command contain User choices, prompt UI for a value
  
  2. If Command contains Directives, loop until all Directives have been resolved and replaced
  
  3. Call Command's run method, paying attention afterwards for any instructions to dump out of the loop early
  
3. If all Commands finished without any early exits, return the pending Commands for the next Step after this one

### Commands

Commands are the internal language of the Utility. Each has a name, and room for parameters, ex: incSkl(<skill>,<value>), and accomplishes a particular effect on the Character, the Environment, or another Command (see subtypes below)

Every Command class will take in, at minimum, a String representing the text version of the Command. The constructor will attempt the validate all the parameters of the Command and return error if it finds fault with any of them. Some parameters can't be evaluated until the Command is run.

#### Directives

A special form of Command that allows for changes to be made or data added to a Command at runtime, just before execution.

The primary use for this is to incorparate either random (dice rolls) or dynamic (character variable) data that can change between runs of the generation process.

Directives do not implement run, they 'resolve', returning their final value so it can be used by the Command containing them.

#### Conditionals

Another subtype of Command that allows for branching decisions to be made based on runtime data. Conditionals examine a simple question, such as "does the character have this skill?", which either leads to the parent command acting differently (branching) or throwing an error (data validation)

Conditionals do not implement run, they 'resolve' returning a true or false value used by their parent Command which decides how to operate from there.

### Step

Steps govern the logical progression of the program, keeping that behavior organized and separate from the other types. Each Step holds a list of effects to run, and a reference to the next Step that should run when the current one finishes.

### Packages/Tables/Functions

Each of these classes is a container for a list of Commands, with differing logic. 

Tables support a list of many possible Command lists, meant to be picked from via a dice roll

Packages also support a list of many possible Command lists 

Functions are parameterized packages. They store a single list of Commands, but can be called with parameters that automatically substitute into those Commands, allowing for more reusability.

### Support packages

#### Wrappers

Defines two major items : AccessWrapper and MappedComponent
  
  * MappedComponent stores a JComponent and an AccessWrapper, tying a UI object to a Data element
    
    * MappedComponents work to pull away from the UI the data model logic needed for manging input and output
    
    * Each MappedComponent has data flow in a particular direction when it's told to update :
    
      * Static components are unchanging
      
      * Pull components update the JComponent with the current value of the Data Element
      
      * Push components set the data element to the current value inputed into the JComponent
    
    * MappedComponents can also refresh, causing them to update their displayed values with the current value stored in their data Element. This happens regardless of whether it is a Push or a Pull element
  
  * AccessWrapper and it's implementations cover a common system for getting and setting data, regardless of the underlying type
  
      * Most major types are covered, simple datatypes as well as several of the application specific objects like Rep, Skill, etc
      
      * Some wrappers contain other wrappers, performing more complex operations on them. SumWrapper returns the int sum of two AccessWrappers
      
      * Some types of AccessWrappers may only support one direction of data flow. Ex: setValue doesn't make sense for SumWrapper
      
  * This allows for short UI that only need to set up the initial structure, after which, the MappedComponents can be looped through and told to manage their updates in response to user input
  
#### UI.Validators

Short package containing Validation classes used by the UI
