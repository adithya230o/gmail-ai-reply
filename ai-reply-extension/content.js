console.log("Extension loaded!");


//track mutations
const observer = new MutationObserver((mutations) => {    //track all mutation and store it as an arr called 'mutations'
    for(const mutation of mutations) {
        const addedNodes = Array.from(mutation.addedNodes);   //.from -> conv NodeList to arr of mutations that were added
        
        const hasComposeElements = addedNodes.some(node => {  //.some -> js method, to check - has atleast one?
            if (node.nodeType !== Node.ELEMENT_NODE) return false;
            
            // Match compose box nodes
            const isComposeMatch = node.matches('.aDh, .btC, [role="dialog"]') || node.querySelector('.aDh, .btC, [role="dialog"]');
            
            if(!isComposeMatch) return false;

            // Check for 'New Message' text inside any compose dialog
            const dialogNode = node.matches('[role="dialog"]') ? node : node.querySelector('[role="dialog"]');
            if (dialogNode) {
                const newMsgSpan = dialogNode.querySelector('.aYF span');
                if (newMsgSpan && newMsgSpan.textContent.includes('New Message')) {
                    console.log("Skipping 'New Message' dialog");
                    return false;
                }
            }

            return true;
        });

        if (hasComposeElements) {
            console.log("Compose Window Detected");
            setTimeout(injectButton, 100);
        }
    }
});

//tells obj what to observe
observer.observe(document.body, {
    childList: true,
    subtree: true
});



//inject button and its functionalities
function injectButton() {

    //removes button added in prev compose window
    const existingButton = document.querySelector('.ai-reply-button');
    if (existingButton) existingButton.remove();

    const toolbar = findComposeToolbar();
    if (!toolbar) {
        console.log("Toolbar not found");
        return;
    }

    console.log("Toolbar found, creating AI button");
    const button = createAIButton();
    button.classList.add('ai-reply-button');

    //track changes in button
    button.addEventListener('click', async () => {
        try {
            button.innerHTML = 'Generating...';
            button.disabled = true;                //to prevent multiple clicks for same mail while waiting for response


            //get emailContent and send it to backend
            const emailContent = getEmailContent();
            const response = await fetch( 'http://localhost:8080/api/email/generate', {
                // (await) -> donot execute until the backend logic is executed
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    emailContent: emailContent
                })
            });

            if (!response.ok) {
                throw new Error('API Request Failed');
            }

            const generatedReply = await response.text();
            // (await) -> donot execute until the reply extracted from raw html content

            const composeBox = document.querySelector('[role="textbox"][g_editable="true"]');

            if (composeBox) {
                //move keyboard's focus to composeBox
                composeBox.focus();
                //execCommand - deprecated
                //showUI -> false (useless, but needs to be passed)
                document.execCommand('insertText', false, generatedReply);
            } else {
                console.error('Compose box was not found');
            }
        } catch (error) {
            console.error(error);
            alert('Failed to generate reply');
        } finally {
            button.innerHTML = 'AI Reply';
            button.disabled =  false;  //enable AI-Reply button again
        }
    });

    toolbar.insertBefore(button, toolbar.firstChild);
}



//find toolbar
function findComposeToolbar() {
    const selectors = [
        '.btC',
        '.aDh',
        '[role="toolbar"]',
        '.gU.Up'
    ];

    for (const selector of selectors) {
        const toolbar = document.querySelector(selector);
        if (toolbar) {
            console.log("found toolbar");
            return toolbar;
        }
    }
    return null;
}


//building AI-reply button
function createAIButton() {
   const button = document.createElement('div');
   button.className = 'T-I J-J5-Ji aoO v7 T-I-atl L3';
   button.style.marginRight = '8px';
   button.style.borderRadius = '20px';
   button.style.backgroundColor = '#0b57d0';
   button.innerHTML = 'AI Reply';
   button.setAttribute('role','button');
   button.setAttribute('data-tooltip','Generate AI Reply');
   return button;
}



//get email content
function getEmailContent() {
    const selectors = [
        '.h7',
        '.a3s.aiL',
        '.gmail_quote',
        '[role="presentation"]'
    ];
    for (const selector of selectors) {
        const content = document.querySelector(selector);
        if (content) {
            return content.innerText.trim();
        }
    }
    return '';
}