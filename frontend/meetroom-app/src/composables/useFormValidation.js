// composables/useFormValidation.js
import { ref, computed } from "vue";

export function useFormValidation() {
	// Email Validation
	const validateEmail = (email, touched = true) => {
		if (!touched) return "";
		if (!email || email.trim() === "") return "Email is required";

		const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
		if (!emailRegex.test(email)) {
			return "Please enter a valid email address";
		}

		return "";
	};

	// Password Validation (⬇️ อัปเดตแล้ว ⬇️)
	const validatePassword = (password, touched = true, minLength = 8) => {
		if (!touched) return "";
		if (!password) return "Password is required";

		if (password.length < minLength) {
			return `Password must be at least ${minLength} characters`;
		}

		if (!/(?=.*[a-z])/.test(password)) {
			return "Must contain at least one lowercase letter";
		}
		if (!/(?=.*[A-Z])/.test(password)) {
			return "Must contain at least one uppercase letter";
		}
		if (!/(?=.*\d)/.test(password)) {
			return "Must contain at least one number";
		}
		if (!/(?=.*[!@#$%^&*(),.?":{}|<>])/.test(password)) {
			return "Must contain at least one special character (@#$%)";
		}

		return "";
	};

	const checkPasswordStrength = (password) => {
		if (!password) return { score: 0, label: "", color: "" };

		let score = 0;
		let label = "Very Weak";
		let color = "bg-red-500";

		if (password.length >= 8) score++;
		if (/(?=.*[a-z])/.test(password)) score++;
		if (/(?=.*[A-Z])/.test(password)) score++;
		if (/(?=.*\d)/.test(password)) score++;
		if (/(?=.*[!@#$%^&*(),.?":{}|<>])/.test(password)) score++;

		switch (score) {
			case 1:
				label = "Weak";
				color = "bg-red-500";
				break;
			case 2:
				label = "Fair";
				color = "bg-orange-500";
				break;
			case 3:
				label = "Medium";
				color = "bg-yellow-500";
				break;
			case 4:
				label = "Good";
				color = "bg-blue-500";
				break;
			case 5:
				label = "Strong";
				color = "bg-green-500";
				break;
			default:
				label = "Very Weak";
				color = "bg-red-500";
		}

		if (password.length === 0) {
			return { score: 0, label: "", color: "bg-gray-200" };
		}

		return { score, label, color };
	};

	// Required Field Validation
	const validateRequired = (
		value,
		fieldName = "This field",
		touched = true
	) => {
		if (!touched) return "";
		if (!value || (typeof value === "string" && value.trim() === "")) {
			return `${fieldName} is required`;
		}
		return "";
	};

	// Min Length Validation
	const validateMinLength = (value, minLength, fieldName, touched = true) => {
		if (!touched) return "";
		if (!value) return "";
		if (value.length < minLength) {
			return `${fieldName} must be at least ${minLength} characters`;
		}
		return "";
	};

	// Max Length Validation
	const validateMaxLength = (value, maxLength, fieldName, touched = true) => {
		if (!touched) return "";
		if (!value) return "";
		if (value.length > maxLength) {
			return `${fieldName} cannot exceed ${maxLength} characters`;
		}
		return "";
	};

	// Number Range Validation
	const validateRange = (value, min, max, fieldName, touched = true) => {
		if (!touched) return "";
		const num = Number(value);
		if (isNaN(num)) return `${fieldName} must be a number`;
		if (num < min) return `${fieldName} must be at least ${min}`;
		if (num > max) return `${fieldName} cannot exceed ${max}`;
		return "";
	};

	// Password Match Validation
	const validatePasswordMatch = (password, confirmPassword, touched = true) => {
		if (!touched) return "";
		if (!confirmPassword) return "Please confirm your password";
		if (password !== confirmPassword) {
			return "Passwords do not match";
		}
		return "";
	};

	return {
		validateEmail,
		validatePassword,
		validateRequired,
		validateMinLength,
		validateMaxLength,
		validateRange,
		validatePasswordMatch,
		checkPasswordStrength,
	};
}
