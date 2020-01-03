import requests
import telebot
from telebot import types
import json
import time

bot = telebot.TeleBot("1068874868:AAFZ7XqAbBtGYnGK6L_JlTck6sOqfT9XOYA")

def send_magic_button(message):
	keyboard = types.InlineKeyboardMarkup()
	keyboard.add(types.InlineKeyboardButton("Magic", callback_data="show_magic"))
	bot.send_message(message.chat.id, "Do you want some magic?", reply_markup=keyboard)

@bot.message_handler(commands=['start', 'help'])
def send_welcome(message):
	send_magic_button(message)

def load_magic_content():
    response = requests.get('https://thesimpsonsquoteapi.glitch.me/quotes')
    if response.status_code == 200:
    	return response.json()[0]
    print("load_magic_content is failed")
    return ""

def send_magic_content(message):
    content = load_magic_content()
    if (content != ""):
    	text = content['quote']
    	character = content['character']
    	image = content['image']
    	bot.send_photo(message.chat.id, image, text + "\n<b>© " + character + "</b>", parse_mode="html")
    else:
    	bot.send_message(message.chat.id, "Sorry, I have received too many requests, please try again later.")	

@bot.message_handler(content_types=['text'])
def handle_text(message):
	print(message.chat.first_name + " " + message.chat.last_name + ": " + message.text)

@bot.callback_query_handler(func=lambda call: True)
def on_magic_callback(call):
	data = call.data
	print(call.from_user.first_name + " " + call.from_user.last_name + ": " + data)
	if (data == "show_magic"):
		send_magic_content(call.message)
		send_magic_button(call.message)

bot.polling(none_stop=True,interval=0)