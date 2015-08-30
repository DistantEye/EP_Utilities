# EP_Utilities
Open Source utility programs for the Eclipse Phase roleplaying system.

##ABOUT

This project started out of a mix of appreciation and frustration for the character generation for Eclipse Phase. The Transhuman book added the Lifepath option, but this became a mess of table flipping and rolling that was overall unwieldy. Thus the goal became first to make a program that could do that in a guided way, then expanded to implementing that goal in as configurable and generalized way as possible.

The code relies on two (semi) human readable data files :
* internalInfo.dat : Contains most RAW building blocks, like skills, rep categories, morphs, sleights, etc
* LifepathPackages.dat : contains advanced structures: packages, tables, steps, etc.

These files can be editted as new game content arrives, or to suit players balance wants and house rules.

While several elements of the code/structure are clearly tied to Eclipse Phase, the backend is intended to be able to be repurposed with limited effort to accomodate other systems. Further revisions of code may make that level of effort even smaller.

The general philosophy of the project is to adhere to a certain level of good practice and standards, with compromises made sometimes to keep momentum up. With the addendum note that code will (and has) get refactored from time to time, with an eventual focus on bringing the code up to full presentation quality.

Bear minimum, you can expect comments and javadocs for most major methods and classes, and hopefully meaningful levels of code organization.

Some items are still a work in progress interms of adhering to proper practices, as I learn more, or realize in hindsight how to do things better, code items get refactored.

##Setup

At this time, this is still a largely work in process project. 

###If you use Eclipse

The project contains a valid .classpath and .project

Use Eclipse's 'Import Projects'->Projects from git

###If you don't use Eclipse
To run it, check out the entire repo, with existing structure left intact.

Compile the src folder with javac

java LifePathUI
(You may have to move the two .dat files from the Data folder into the same directory as LifePathUI)

##RUNABLE PROGRAMS

1) LifePathUI : Only current program. Runs a no-thrills GUI interface that allows players to move through each step, prompting for choices as necessary, by hitting 'Run Next Step'

##Commands

LifepathPackages.dat uses a limited effect/command system, allowing users some ability to create advanced behavior when customizing the file contents. A general guide on these can be found in CommandsManual.txt.

##DATA NOTE/LEGAL
The .dat files for this program contain Eclipse Phase content, 
which is liscensed under the below

>Posthuman Studios is: Rob Boyle, Brian Cross, Adam Jury

>Creative Commons License; Some Rights Reserved.

>This work is licensed under the Creative Commons Attribution-

>Noncommercial-Share Alike 3.0 Unported License. 

>To view a copy of this license, visit:

>http://creativecommons.org/licenses/by-nc-sa/3.0/ 

>or send a letter to: Creative Commons, 171 Second Street, 

>Suite 300, San Francisco, California, 94105, USA.

>What this means is that you are free to copy, share, and 

>remix the text and artwork within this book under the fol-

>lowing conditions: 

>1) you do so only for noncommercial purposes; 

>2) you attribute Posthuman Studios; 

>3) you license any derivatives under the same license. 

>For specific details, appropriate credits, and updates/changes 

>to this license, please see: http://eclipsephase.com/cclicense
