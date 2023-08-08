$(document).ready(() => {
    $('[data-bs-toggle=tooltip]').tooltip();

    new Choices('#selected-locations', {
        removeItemButton: true,
        maxItemCount: 5,
        searchResultLimit: 5,
        renderChoiceLimit: 5
    });

    new Choices('#selected-industries', {
        removeItemButton: true,
        maxItemCount: 5,
        searchResultLimit: 5,
        renderChoiceLimit: 5,
    });

    getProfilesSearches();
    document.getElementById("start-profile-scraper").addEventListener("click", (event) => startProfileScraper(event));
    setInterval(getStatus, 200);

    // const searchInput = document.getElementById("search-title-input");
   

})
let statusMessages = []
let searches = new Set();
let searchesId = new Set();


const getProfilesSearches = () => {
    fetch('/api/v1/profiles-searches')
        .then((response) => response.json()).then((data) => {
            if (data.length < 1) {
                document.getElementById("searches-table").style.display = "none";
                document.getElementById("no-searches-indicator").style.display = "block";
            }
            const tableBody = document.getElementById("profiles-searches-tbody");
            data.forEach((search, index) => {
                if (!searchesId.has(search.id)) {
                    searches.add(search);
                    searchesId.add(search.id);
                    const newRow = tableBody.insertRow();
                    newRow.innerHTML = `
                   <td>${index + 1}</td>
                   <td>${search.title}</td>
                   <td>${getFormattedDate(search.searchedAt)}</td>
                   <td><button class="btn btn-sm btn-primary" onclick="viewProfiles('${search.id}')" >View Profiles</button></td>
               `;
                }
            });
        })
        .catch((error) => {
            console.log("Error fetching profiles searches:", error);
        });
};


const viewProfiles = (searchId) => {
    fetch(`/api/v1/view-profiles/${searchId}`).then((response) => response.json()).then((data) => {
        const tableBody = document.getElementById("profiles-by-search-result");
        tableBody.innerHTML = ''; // Clear existing rows before adding new profiles
        data.forEach((profile, index) => {
            const newRow = tableBody.insertRow();
            newRow.innerHTML = `
            <td>${index + 1}</td>
            <td>${profile.name}</td>
            <td>${profile.email}</td>
            <td>${profile.about}</td>
            <td class="experience-th" title="${profile.experience}"><button id="${index}-copyExperienceBtn" type="button" class="btn btn-success btn-sm mb-1" data-copytext="${profile.experience}" onclick="copyToClipBoard('${index}-copyExperienceBtn')" >Copy experience</button><button type="button" class="btn btn-sm btn-primary" >View Experience</button></td>
            <td class="education-th" title="${profile.education}" ><button id="${index}-copyEducationBtn" type="button" class="btn btn-success btn-sm" data-copytext="${profile.education}" onclick="copyToClipBoard('${index}-copyEducationBtn')" >Copy education</button> <button type="button" class="btn btn-sm btn-primary mt-1">View Education</button></td>
            <td class="text-center" >${profile.isOpenToWork ? "Yes" : "No"}</td>
            <td><a href="${profile.link}"  target="_blank" >Open Profile</a></td>
            `;
        })
        $('#profiles-by-search-modal').modal("show");
    }).catch((error) => {
        console.log("Unable to get profiles by search, error: ", error);
    })
}


const getFormattedDate = (date) => {
    date = new Date(date);
    return `${padWithZero(date.getDate())}/${padWithZero(date.getMonth() + 1)}/${date.getFullYear()} - ${padWithZero(date.getHours())}:${padWithZero(date.getMinutes())}`;

    function padWithZero(number) {
        return number.toString().padStart(2, '0');
    }
}

const startProfileScraper = (event) => {
    event.preventDefault();

    let selectedLocations = []
    let selectedIndustries = []

    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const keywords = document.getElementById("keywords").value;
    const title = document.getElementById("title").value;

    const totalProfilesToFetch = document.getElementById("totalProfilesToFetch").value;

    const firstConnection = document.getElementById('con1').checked;
    const secondConnection = document.getElementById('con2').checked;
    const thirdConnection = document.getElementById('con3').checked;

    console.log("first connection checked: ", firstConnection)

    const locations = document.getElementById("selected-locations").selectedOptions;
    for (let option of locations) {
        selectedLocations.push(option.value);
    }

    const industries = document.getElementById("selected-industries").selectedOptions;
    for (let option of industries) {
        selectedIndustries.push(option.value)
    }

    const requestedData = {
        email: email,
        password: password,
        title: title,
        keywords: keywords,
        totalProfilesToFetch: totalProfilesToFetch,
        headlessMode: false,
        filters: {
            isFirstConnectionChecked: firstConnection,
            isSecondConnectionChecked: secondConnection,
            isThirdConnectionChecked: thirdConnection,
            selectedLocations: selectedLocations,
            selectedIndustries: selectedIndustries,
        }
    };


    fetch('/api/v1/profile-scraper', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestedData)
    }).then((response) => response.text()).then((response) => {
        getStatus();
    }).catch((error) => {
        console.log(error);
    })
}

const copyToClipBoard = (id) => {
    const copyBtn = document.getElementById(id);
    const copyText = copyBtn.getAttribute("data-copytext");
    copyBtn.addEventListener("click", function () {
        navigator.clipboard.writeText(copyText);
        alert("Copied successfully!")
    });
}

const getStatus = () => {
    fetch('/api/v1/status/profile-scraper').then((response) => response.json())
        .then((response) => {

            const statusDiv = document.getElementById("scraper-live-messages");

            if (response.scraperRunning) {
                document.getElementById("scraper-live-messages").innerHTML = '';
                document.getElementById("start-profile-scraper").disabled = true;

                if (!statusMessages.includes(response.status)) {
                    statusMessages.push(response.status);
                }

                let formattedMessages = statusMessages.map((message, index) => `${index + 1}. ${message}`);
                statusDiv.innerHTML = formattedMessages.join('<br>');

            } else {
                document.getElementById("scraper-live-messages").innerHTML = 'Currently scraper is not running.';
                document.getElementById("start-profile-scraper").disabled = false;
            }

            if (response.scrapedSuccess) {
                document.getElementById("scraper-live-messages").innerHTML = 'Successfully saved fetch profiles into database.';
                document.getElementById("start-profile-scraper").disabled = false;
                getProfilesSearches();
                document.getElementById("scraper-live-messages").innerHTML = "Currently scraper is not running.";
                swal("Success", "Successfully fetched and saved profiles to database.", "success");
                $('#profile-scraper-modal').modal("hide");

            }

        }).catch((error) => {
            console.log("Error getting status: ", error)
        })
}

    
function filterSearches() {
    const enteredKeywords = document.getElementById("search-title-input").value.toLowerCase();
    const tableBody = document.getElementById("profiles-searches-tbody");
    console.log("entered keywords: ", enteredKeywords)

    const searchesSet = Array.from(searches);
    tableBody.innerHTML = '';

    
    searchesSet.forEach((search, index) => {
        title = search.title.toLowerCase();
        if (title.includes(enteredKeywords) || enteredKeywords === "") {
            const newRow = tableBody.insertRow();
                    newRow.innerHTML = `
                   <td>${index + 1}</td>
                   <td>${search.title}</td>
                   <td>${getFormattedDate(search.searchedAt)}</td>
                   <td><button class="btn btn-sm btn-primary" onclick="viewProfiles('${search.id}')" >View Profiles</button></td>
               `;
        }
    });
}