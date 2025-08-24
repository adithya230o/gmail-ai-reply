console.log("Extension loaded!");

const PREWARM_INTERVAL = 13 * 60 * 1000;

async function prewarmBackend() {
    try {
        await fetch('http://localhost:8082/api/email/warmup');
        console.log("Backend prewarm call sent");
        chrome.storage.local.set({ lastPrewarm: Date.now() });
    } catch (err) {
        console.error("Prewarm call failed", err);
    }
}

// Gmail tab loaded → fire prewarm immediately
prewarmBackend();

// Keep backend alive while Gmail tab is open
setInterval(prewarmBackend, PREWARM_INTERVAL);

//track mutations
const observer = new MutationObserver((mutations) => {
    for (const mutation of mutations) {                         //iterate through every mutation

        const addedNodes = Array.from(mutation.addedNodes);     // Nodelist of newly added mutations

        const hasComposeElements = addedNodes.some(node =>      //.some -> has atleast one ele that matches condition?
            node.nodeType === Node.ELEMENT_NODE &&
            (node.matches('.aDh, .btC, [role="dialog"]') || node.querySelector('.aDh, .btC, [role="dialog"]'))
        );

        if (hasComposeElements) {
            const containsVisibleSubjectBox = addedNodes.some(node => {         //to check if it is a new compose box
                if (node.nodeType !== Node.ELEMENT_NODE) return false;

                const subjectInput = node.matches('input[name="subjectbox"]')
                    ? node
                    : node.querySelector('input[name="subjectbox"]');

                if (!subjectInput) return false;

                //checks if the subject field is visible on screen(might ne invisble for replies)
                const style = window.getComputedStyle(subjectInput);
                return style.display !== 'none' && style.visibility !== 'hidden' && subjectInput.offsetParent !== null;
            });

            if (containsVisibleSubjectBox) {
                console.log("New mail -> skipping injection");
                continue;
            }

            console.log("Reply Window -> inject 'AI Reply' button");
            setTimeout(injectButton, 100);
        }
    }
});

//tells observer what things to observe
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

    console.log("Creating AI button");
    const button = createAIButton();
    button.classList.add('ai-reply-button');

    //track changes in button
    button.addEventListener('click', async () => {

        const startTime = performance.now();  // Start timer

        try {
            button.innerHTML = 'Generating...';
            button.disabled = true;                //to prevent multiple clicks for same mail while waiting for response


            //get emailContent and send it to backend
            const emailContent = getEmailContent();
            const response = await fetch( 'http://localhost:8082/api/email/generate', {
                // (await) -> donot execute until the backend logic is executed
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    emailContent: emailContent
                })
            });

            if (response.status === 429) {
                const errorMsg = await response.text();

                if (errorMsg === "Minute limit exceeded") {
                    alert("Limit of 15 requests per minute reached. Please wait 1 minute and try again.");
                } else if (errorMsg === "Daily limit exceeded") {
                    alert("Daily limit of 200 requests reached. Try again tomorrow.");
                } else {
                    alert("Rate limit reached. Try again later.");
                }

                return;
            }

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

            const endTime = performance.now();  // End timer
            const timeTaken = ((endTime - startTime) / 1000).toFixed(2);  // in seconds
            alert(`Reply generated in ${timeTaken} seconds`);

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
            console.log("Found toolbar");
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
   button.setAttribute('data-tooltip','Genemail-ai-reply-extrate AI Reply');
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