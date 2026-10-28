document.addEventListener("click", async function (e) {
    // =========================
    // 🔹 DELETE IMAGE
    // =========================
    const deleteBtn = e.target.closest(".img-delete");

    if (deleteBtn) {

        const imageId = deleteBtn.dataset.id;

        if (!imageId) return;

        if (!confirm("Удалить изображение?")) return;

        try {

            const response = await fetch(`/work/image/${imageId}/delete`, {
                method: "POST"
            });

            if (response.ok) {

                deleteBtn.closest(".img-wrapper")?.remove();

                loadImages(currentWorkId);
            }

        } catch (err) {
            console.error("DELETE ERROR:", err);
        }
    }
});

function toggle(btn) {
    const container = btn.closest(".sidebar-block");
    const list = container.querySelector("div[style*='display']");

    if (!list) return;

    const isHidden = list.style.display === "none" || list.style.display === "";

    list.style.display = isHidden ? "block" : "none";

    btn.classList.toggle("open", isHidden);
}

function toggleSection(button) {
    const container = button.closest(".section-header").nextElementSibling;
    const isOpen = container.style.display === "block";

    container.style.display = isOpen ? "none" : "block";
    button.classList.toggle("open", !isOpen);
}

let currentWorkId = null;
let currentUstanovkaId = null;
let selectedFile = null;

document.addEventListener("DOMContentLoaded", function () {

    const fileInput = document.getElementById("fileInput");
    const preview = document.getElementById("preview");
    const uploadBtn = document.getElementById("uploadBtn");
    const status = document.getElementById("uploadStatus");
    const gallery = document.getElementById("gallery");

    // =========================
    // 🔹 ОТКРЫТИЕ МОДАЛКИ
    // =========================
    document.querySelectorAll('.open-work-modal').forEach(btn => {

        btn.addEventListener('click', function () {

            const workId = this.dataset.id;

            fetch('/api/work/' + workId)
                .then(res => res.json())
                .then(work => {

                    currentWorkId = work.id;
                    currentUstanovkaId = work.ustanovka?.id;

                    // очистка галереи
                    if (gallery) gallery.innerHTML = '';

                    const form = document.getElementById('editWorkForm');

                    // заполнение формы
                    form.querySelectorAll('[name]').forEach(field => {

                        const key = field.name;
                        let value = work[key];

                        if (value === null || value === undefined) value = '';

                        if (field.type === 'checkbox') {
                            field.checked = value === true;
                        } else {
                            field.value = value;
                        }
                    });

                    if (work.ustanovka) {
                        document.getElementById('ustanovka_id').value = work.ustanovka.id;
                        document.getElementById('ustanovka_name').value = work.ustanovka.name;
                    }

                    form.action = '/work/' + work.id + '/update';

                    loadImages(work.id); // 👈 ВОТ ЭТО ОБЯЗАТЕЛЬНО

                    // пример — если у тебя есть поле work.done
                    const checkboxes = document.querySelectorAll(".done-checkbox");

                    checkboxes.forEach(cb => {
                        cb.checked = work.done === true;
                    });
                    console.log(work);
                });
        });
    });

    // =========================
    // 🔹 ВЫБОР ФАЙЛА
    // =========================
    if (fileInput) {
        fileInput.addEventListener("change", function () {

            selectedFile = this.files[0];
            if (!selectedFile) return;

            const reader = new FileReader();
            reader.onload = e => {
                preview.src = e.target.result;
                preview.style.display = "block";
            };
            reader.readAsDataURL(selectedFile);
        });
    }

    // =========================
    // 🔹 DRAG & DROP
    // =========================
    const dropArea = document.getElementById("drop-area");

    if (dropArea) {
        dropArea.addEventListener("drop", function (e) {
            e.preventDefault();

            const file = e.dataTransfer.files[0];
            if (!file) return;

            selectedFile = file;

            const reader = new FileReader();
            reader.onload = ev => {
                preview.src = ev.target.result;
                preview.style.display = "block";
            };
            reader.readAsDataURL(file);
        });
    }

    document.addEventListener("dragover", e => e.preventDefault());
    document.addEventListener("drop", e => e.preventDefault());

    // =========================
    // 🔹 UPLOAD
    // =========================
    if (uploadBtn) {
        uploadBtn.addEventListener("click", async () => {

            console.log("UPLOAD CLICK", currentWorkId, selectedFile);

            if (!selectedFile) {
                alert("Сначала выбери файл");
                return;
            }

            if (!currentWorkId) {
                alert("Работа не выбрана");
                return;
            }

            const formData = new FormData();
            formData.append("files", selectedFile);

            try {
                const response = await fetch(`/work/${currentWorkId}/upload`, {
                    method: "POST",
                    body: formData
                });

                const data = await response.json();

                if (response.ok) {

                    const imageUrl = `/uploads/works/${currentUstanovkaId}/${data.fileName}`;

                    const gallery = document.getElementById('gallery');

                    if (!gallery) return;

                    // 🔥 создаем элемент как нужно для lightbox
                    const wrapper = document.createElement("div");
                    wrapper.className = "img-wrapper";

                    const link = document.createElement("a");
                    link.href = imageUrl;
                    link.setAttribute("data-lightbox", "gallery");

                    const img = document.createElement("img");
                    img.src = imageUrl;
                    img.style.maxHeight = "65px";

                    const del = document.createElement("span");
                    del.className = "img-delete";
                    del.innerText = "✖";
                    del.dataset.id = data.id;

                    link.appendChild(img);
                    wrapper.appendChild(link);
                    wrapper.appendChild(del);

                    // gallery.appendChild(wrapper);
                    loadImages(currentWorkId);

                    // 🔥 оставляем только 2 последних
                    const wrappers = gallery.querySelectorAll(".img-wrapper");
                    if (wrappers.length > 2) {
                        wrappers[0].remove();
                    }

                    // 🔥 lightbox refresh
                    if (window.lightbox) {
                        lightbox.reload?.();
                    }

                    // статус
                    if (status) {
                        status.style.display = "block";
                        setTimeout(() => status.style.display = "none", 2000);
                    }

                    // очистка
                    preview.src = "";
                    preview.style.display = "none";
                    selectedFile = null;
                    fileInput.value = "";
                }

            } catch (err) {
                console.error("UPLOAD ERROR:", err);
            }
        });
    }

});

// =========================
// 🔹 ЗАГРУЗКА ИЗОБРАЖЕНИЙ
// =========================
function loadImages(workId) {

    fetch(`/api/work/${workId}/images`)
        .then(res => res.json())
        .then(images => {

            const gallery = document.getElementById('gallery');
            if (!gallery) return;

            gallery.innerHTML = '';

            // 🔥 контейнеры
            const visibleContainer = document.createElement("div");
            const hiddenContainer = document.createElement("div");
            hiddenContainer.style.display = "none";

            images.forEach((img, index) => {

                const imageUrl = `/uploads/works/${currentUstanovkaId}/${img.fileName}`;

                // 🔗 ссылка для lightbox
                const link = document.createElement("a");
                link.href = imageUrl;
                link.setAttribute("data-lightbox", "gallery");

                const image = document.createElement("img");
                image.src = imageUrl;
                image.style.maxHeight = "65px";

                const del = document.createElement("span");
                del.className = "img-delete";
                del.innerText = "✖";
                del.dataset.id = img.id;

                link.appendChild(image);

                // 🔥 последние 2 — показываем
                if (index >= images.length - 2) {

                    const wrapper = document.createElement("div");
                    wrapper.className = "img-wrapper";

                    wrapper.appendChild(link);
                    wrapper.appendChild(del);

                    visibleContainer.appendChild(wrapper);

                } else {
                    // 🔥 остальные — скрытые
                    hiddenContainer.appendChild(link);
                }
            });

            gallery.appendChild(visibleContainer);
            gallery.appendChild(hiddenContainer);

            // 🔥 lightbox
            if (window.lightbox) {
                lightbox.reload?.();
            }
        });
}

// Чекбокс - журналы
document.addEventListener("submit", function (e) {

    const form = e.target;

    if (!form.matches("#editWorkForm")) return;

    const checkboxes = form.querySelectorAll(".done-checkbox");

    if (checkboxes.length === 0) return;

    const allChecked = Array.from(checkboxes).every(cb => cb.checked);

    const hidden = form.querySelector("input[name='done']");

    if (hidden) {
        hidden.value = allChecked ? "true" : "false";
    }

    console.log("DONE =", hidden.value); // для проверки
});


